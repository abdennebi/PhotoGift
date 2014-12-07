package com.abdennebi.photogift.application;


import java.util.List;

import com.abdennebi.photogift.domain.GiftChain;

public class Listener {

    public interface GiftChainListListener {

        void onReceived(List<GiftChain> giftChainList);
    }

    public interface GiftChainListener {

        void onReceived(GiftChain giftChain);
    }

    public interface GiftChainSearchListener {

        void onReceived(GiftChain giftChain);
    }

}
