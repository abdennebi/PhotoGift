package photogift.server.resource;


import photogift.server.domain.GiftChain;
import photogift.server.service.PagedResult;

import java.util.ArrayList;
import java.util.List;

public class PagedGiftChainAssembler {

    public static List<GiftChainResource> toResource(PagedResult<GiftChain> pagedResult) {

        List<GiftChainResource> giftChainResources = new ArrayList<GiftChainResource>();

        ArrayList<GiftChain> giftChainList = pagedResult.getContent();

        for (GiftChain GiftChain : giftChainList) {
            giftChainResources.add(GiftChainResource.Assembler.toResource(GiftChain));
        }

        return giftChainResources;
    }
}
