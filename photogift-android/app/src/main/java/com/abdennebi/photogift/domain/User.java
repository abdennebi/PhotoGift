package com.abdennebi.photogift.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class User implements Parcelable {

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public Long userId;
    public String googleUserId;
    public String googleDisplayName;
    public String googlePhotoUrl;
    public String googleProfileUrl;
    public Date lastUpdated;
    public List<Long> touchingGifts;

    public User() {
    }

    public User(Parcel parcel) {
        userId = parcel.readLong();
        googleUserId = parcel.readString();
        googleDisplayName = parcel.readString();
        googlePhotoUrl = parcel.readString();
        googleProfileUrl = parcel.readString();
        lastUpdated = (Date) parcel.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(userId);
        parcel.writeString(googleUserId);
        parcel.writeString(googleDisplayName);
        parcel.writeString(googlePhotoUrl);
        parcel.writeString(googleProfileUrl);
        parcel.writeSerializable(lastUpdated);
    }
}
