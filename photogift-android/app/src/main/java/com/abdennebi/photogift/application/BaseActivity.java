package com.abdennebi.photogift.application;


import android.app.Activity;

public abstract class BaseActivity extends Activity {


    public PhotoGift application() {
        return (PhotoGift) getApplication();
    }

    public ApplicationSession session() {
        return application().getApplicationSession();
    }

}
