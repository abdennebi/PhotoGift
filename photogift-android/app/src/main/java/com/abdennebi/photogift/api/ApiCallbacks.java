package com.abdennebi.photogift.api;

import com.abdennebi.photogift.domain.User;

public class ApiCallbacks {

    public interface ServiceCallback {

        void onUserRetrieved(User user);

        void codeSignInRequired();

        void onSignOut();
    }

    public interface Callback<T> {

        void onSuccess(T result);

        void onFailure(Exception ex);
    }

}
