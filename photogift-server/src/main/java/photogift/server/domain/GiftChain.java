package photogift.server.domain;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Cache
public class GiftChain {

    public static String kind = "photogift#giftchain";


    public static Key<GiftChain> key(long id) {
        return Key.create(GiftChain.class, id);
    }

    @Id
    public Long id;

    /**
     * The URL of the of the thumbnail of the first photo.
     */
    public String thumbnailUrl;

    public Date creationDate;

    /**
     * The title of chain borrowed from the first gift.
     */
    public String title;

    /**
     * The list of ids of gifts that belong to this chain
     */
    public List<Long> giftIds = new ArrayList<Long>();

    @Ignore
    /**
     * This list is populated while reading a GiftChain from the datastore.
     */
    public List<Gift> giftList;

    public Date updateDate;
}
