package com.abdennebi.photogift.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class Gift implements Serializable {

    public String title;

    public String text;

    public String imageUrl;

    public String thumbnailUrl;

    public Long giftId;

    public Long giftChainId;

    public String ownerDisplatName ="";

    public String ownerProfileUrl;

    public String ownerProfilePhoto;

    public boolean inappropriate;

    public int touchedCount;

    public Date creationDate;
}
