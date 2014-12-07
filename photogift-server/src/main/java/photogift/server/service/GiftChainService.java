package photogift.server.service;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import photogift.server.domain.Gift;
import photogift.server.domain.GiftChain;
import photogift.server.exception.NotFoundException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static photogift.server.config.OfyService.ofy;

@Service
public class GiftChainService {

    @Autowired
    private GiftService giftService;


    public PagedResult<GiftChain> readWithPaging(@Nullable String current) {
        final boolean ascending = true;
        return readWithPaging(current, 1000, ascending);

    }

    public PagedResult<GiftChain> readPreviousWithPaging(String next) {

        Assert.notNull(next);

        final boolean descending = false;

        PagedResult<GiftChain> result = readWithPaging(next, 2, descending);

        result.reverse();

        return result;
    }

    public GiftChain read(Long id, boolean doNotShowInappropriate) {

        GiftChain giftChain = ofy().load().type(GiftChain.class).id(id).now();

        if (giftChain == null) {
            throw new NotFoundException("Entiy with id : " + id + " not found");
        }

        List<Long> giftIds = giftChain.giftIds;

        giftChain.giftList = giftService.loadGifts(giftIds, doNotShowInappropriate);

        return giftChain;
    }



    private PagedResult<GiftChain> readWithPaging(@Nullable String current, int limit, boolean ascending) {

        final ArrayList<GiftChain> giftChains = new ArrayList<GiftChain>();

        Query<GiftChain> query = ofy().load().type(GiftChain.class).limit(limit).orderKey(ascending);

        if (current != null) {
            query = query.startAt(Cursor.fromWebSafeString(current));
        }

        final QueryResultIterator<GiftChain> iterator = query.iterator();

        // Do not use for each loop otherwise the cursor will be empty
        while (iterator.hasNext()) {
            GiftChain chain = iterator.next();
            giftChains.add(chain);
        }

        final Cursor next = iterator.getCursor();

        return new PagedResult<GiftChain>(giftChains, current, next.toWebSafeString());
    }
}
