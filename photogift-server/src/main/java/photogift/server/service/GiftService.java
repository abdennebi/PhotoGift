package photogift.server.service;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.search.*;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.cmd.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import photogift.server.domain.*;
import photogift.server.exception.BadStateException;
import photogift.server.exception.NotFoundException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static photogift.server.config.OfyService.ofy;

@Service
public class GiftService {

    @Autowired
    private ImagesService imagesService;

    @Autowired
    private UserService userService;

    @Autowired
    private Index giftIndex;

    /**
     * Setup photoContentUrl after this Photo has been loaded.
     */
    @OnLoad
    protected void photoDeepLinkUrl(Gift gift) {
        gift.photoContentUrl = "/photo.html?photoId=" + gift.id;
    }

    /**
     * Creates a new {@link Gift}.
     *
     * @param title        the title of the Gift
     * @param text         the accompagning text
     * @param giftChainId  the Gift Chain who the gift belongs to
     * @param imageBlobKey the key into blobstore of the image file
     * @param user         the user who created the gift
     * @return the newly created gift
     */
    public Gift create(String title, @Nullable String text, @Nullable Long giftChainId, String imageBlobKey, User user) {

        GiftChain giftChain;

        String thumbnailUrl = getImageUrl(imageBlobKey, Gift.DEFAULT_THUMBNAIL_SIZE);

        final Date now = new Date();

        if (giftChainId == null) {
            // this is a new Gift Chain
            giftChain = new GiftChain();
            giftChain.giftIds = new ArrayList<Long>();
            giftChain.creationDate = now;
            giftChain.updateDate = now;
            giftChain.title = title;
            giftChain.thumbnailUrl = thumbnailUrl;
            ofy().save().entity(giftChain).now();
            ofy().clear();
            giftChain = ofy().load().type(GiftChain.class).id(giftChain.id).now();
        } else {

            giftChain = ofy().load().type(GiftChain.class).id(giftChainId).now();

            if (giftChain == null) throw new BadStateException();

            giftChain.updateDate = now;
            ofy().save().entity(giftChain).now();
            ofy().clear();
        }

        Gift gift = new Gift();
        gift.text = text;
        gift.title = title;
        gift.ownerUserId = user.id;
        gift.ownerDisplayName = user.googleDisplayName;
        gift.ownerProfilePhoto = user.googlePhotoUrl;
        gift.ownerProfileUrl = user.googleProfileUrl;
        gift.created = Calendar.getInstance().getTime();
        gift.imageBlobKey = imageBlobKey;
        gift.fullsizeUrl = getImageUrl(imageBlobKey);
        gift.thumbnailUrl = thumbnailUrl;
        gift.giftChainId = giftChain.id;

        ofy().save().entity(gift).now();
        ofy().clear();
        gift = ofy().load().type(Gift.class).id(gift.id).now();

        giftChain.giftIds.add(gift.id);
        ofy().save().entity(giftChain).now();
        ofy().clear();

        index(gift);

        return gift;
    }

    /**
     * @return URL for full-size image of this photo.
     */
    public String getImageUrl(String imageBlobKey) {
        return getImageUrl(imageBlobKey, -1);
    }

    /**
     * @param size Size of image for URL to return.
     * @return URL for images for this Photo of given size.
     */
    public String getImageUrl(String imageBlobKey, int size) {
        ServingUrlOptions options = ServingUrlOptions.Builder
                .withBlobKey(new BlobKey(imageBlobKey))
                .secureUrl(true);
        if (size > -1) {
            options.imageSize(size);
        }
        return imagesService.getServingUrl(options);
    }


    public Gift read(Long id) {

        Gift gift = ofy().load().type(Gift.class).id(id).now();

        if (gift == null) {
            throw new NotFoundException("Photo with id : " + id + " not found");
        }

        gift.touchedCount = getTouchedCount(id);

        return gift;
    }

    public void setTouched(Long giftId, User user) {

        List<TouchingGift> userAlreadyTouched = ofy().load().type(TouchingGift.class)
                .filter("touchedUserId =", user.id)
                .filter("giftId =", giftId)
                .list();

        if (userAlreadyTouched.size() >= 1) throw new BadStateException("User already voted");

        TouchingGift touchingGift = new TouchingGift();
        touchingGift.giftId = giftId;
        touchingGift.touchedUserId = user.id;
        ofy().save().entity(touchingGift).now();

        // TODO Top Givers
        Gift read = read(giftId);
        Long ownerUserId = read.ownerUserId;

        User giftOwner = userService.loadUser(ownerUserId);

        TopGiver topGiver = ofy().load().type(TopGiver.class).id(ownerUserId).now();

        if (topGiver == null) {
            topGiver = new TopGiver();
            topGiver.ownerUserId = ownerUserId;
            topGiver.count = 1;
            topGiver.googleDisplayName = giftOwner.googleDisplayName;
            topGiver.googlePhotoUrl = giftOwner.googlePhotoUrl;
        }
        topGiver.count = topGiver.count + 1;
        ofy().save().entity(topGiver).now();
        ofy().clear();

    }

    public int getTouchedCount(Long giftId) {
        return ofy().load().type(TouchingGift.class)
                .filter("giftId =", giftId)
                .count();
    }

    public List<TopGiver> getTopGivers() {

        final ArrayList<TopGiver> givers = new ArrayList<TopGiver>();

        Query<TopGiver> query = ofy().load().type(TopGiver.class).limit(10).order("count");

        final QueryResultIterator<TopGiver> iterator = query.iterator();

        while (iterator.hasNext()) {
            TopGiver giver = iterator.next();
            givers.add(giver);
        }

        return givers;
    }

    private void index(Gift gift) {
        Document.Builder docBuilder = Document.newBuilder().setId(gift.id.toString())
                .addField(Field.newBuilder().setName("title").setText(gift.title));

        Document doc = docBuilder.build();
        giftIndex.put(doc);
    }

    public List<Gift> search(String queryStr, boolean doNotShowInappropriate) {

        return loadGifts(searchIntoIndex(queryStr), doNotShowInappropriate);
    }

    public List<Gift> loadGifts(List<Long> giftIds, boolean doNotShowInappropriate) {

        List<Gift> giftList = new ArrayList<Gift>();

        if (giftIds.size() >= 1)

            // read in the reverse order to get the more recent in the head of the list
            for (int i = giftIds.size() - 1; i >= 0; i--) {

                Gift gift = read(giftIds.get(i));

                if (!gift.inappropriate || !doNotShowInappropriate) {
                    giftList.add(gift);
                }
            }
        return giftList;
    }

    private List<Long> searchIntoIndex(String queryStr) {
        int limit = 100;
        List<Long> found = new ArrayList<Long>();

        com.google.appengine.api.search.Query query = com.google.appengine.api.search.Query.newBuilder().
                setOptions(QueryOptions.newBuilder().setLimit(limit).build()).build(queryStr);

        Results<ScoredDocument> results = giftIndex.search(query);
        for (ScoredDocument scoredDoc : results) {
            String id = scoredDoc.getId();
            found.add(Long.valueOf(id));
        }
        return found;
    }

    public void setInappropriate(Long id, User user) {

        int alreadyFlagged = ofy().load().type(InappropriateGift.class)
                .filter("flaggedByUserId", user.id)
                .filter("giftId", id)
                .count();

        if (alreadyFlagged >= 1) throw new BadStateException("User has already flagged this gift as inappropriate");

        // if more thant two users tag a gift as inappropriate then it's considered as inappropriate
        int markAsInappropriateThreshold = 2;

        InappropriateGift inappropriateGift = new InappropriateGift();
        inappropriateGift.flaggedByUserId = user.id;
        inappropriateGift.giftId = id;

        ofy().save().entity(inappropriateGift).now();


        int inappropriateCount = ofy().load().type(InappropriateGift.class)
                .filter("giftId", id)
                .count();

        if (inappropriateCount >= markAsInappropriateThreshold) {
            // We mark Gift as definitevely as inappropriate
            Gift gift = read(id);

            if (!gift.inappropriate) {
                gift.inappropriate = true;
                ofy().save().entity(gift).now();
            }
        }
    }
}
