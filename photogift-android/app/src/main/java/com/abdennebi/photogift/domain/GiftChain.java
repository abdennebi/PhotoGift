package com.abdennebi.photogift.domain;

import java.util.Date;
import java.util.List;

public class GiftChain {

    public Long giftChainId;

    public String title;

    public String thumbnailUrl;

    public Date creationDate;

    public Date updateDate;

    public List<Gift> giftList;
}
