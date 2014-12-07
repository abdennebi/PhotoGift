package com.abdennebi.photogift.application;

import android.app.Application;

import com.abdennebi.photogift.api.ApiClient;
import com.abdennebi.photogift.api.retrofit.ApiClientImpl;
import com.abdennebi.photogift.config.Constants;


public class PhotoGift extends Application {

    private ApplicationSession session;

    private ApiClient apiClient;

    @Override
    public void onCreate() {
        session = ApplicationSession.getSessionForServer(this, Constants.OAuth2.OAUTH_SERVER_CLIENT_ID);
        apiClient = new ApiClientImpl(session);
    }

    public ApplicationSession getApplicationSession() {
        return session;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public boolean isAuthenticated() {
        return session.getUser() != null;
    }

}
