package com.abdennebi.photogift.application;

import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {

    public PhotoGift application() {
        return (PhotoGift) getApplication();
    }

    public ApplicationSession session() {
        return application().getApplicationSession();
    }

}
