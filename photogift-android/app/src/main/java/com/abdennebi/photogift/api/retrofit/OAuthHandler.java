package com.abdennebi.photogift.api.retrofit;

import android.util.Log;

import com.abdennebi.photogift.application.ApplicationSession;
import static com.abdennebi.photogift.config.Constants.Headers.*;
import retrofit.RequestInterceptor;

public class OAuthHandler implements RequestInterceptor {

    private static final String TAG = OAuthHandler.class.getSimpleName();

    private final ApplicationSession mApplicationSession;

    public OAuthHandler(ApplicationSession applicationSession) {
        mApplicationSession = applicationSession;
    }

    /**
     * Every time a method on the client interface is invoked, this method is
     * going to get called. The method checks if the client has previously obtained
     * an OAuth 2.0 bearer token. If not, the method obtains the bearer token by
     * sending a password grant request to the server.
     * <p/>
     * Once this method has obtained a bearer token, all future invocations will
     * automatically insert the bearer token as the "Authorization" header in
     * outgoing HTTP requests.
     */
    @Override
    public void intercept(RequestFacade request) {

        request.addHeader(HEADER_USER_AGENT, USER_AGENT);

        if (mApplicationSession == null) {
            return;
        }

        if (mApplicationSession.getCode() != null) {
            request.addHeader(HEADER_XOAUTH, mApplicationSession.getCode());
            // Don't want to send the same code twice, nullify after sending once
            mApplicationSession.setCode(null);
        }

        if (mApplicationSession.getSessionId() != null) {
            request.addHeader(HEADER_COOKIE, COOKIE_PREFIX + mApplicationSession.getSessionId());
        } else if (mApplicationSession.getAccountName() != null) {
            String idToken = mApplicationSession.getIdTokenSynchronous();
            if (idToken != null) {
                request.addHeader(HEADER_AUTH, HEADER_BEARER + idToken);
            } else {
                Log.d(TAG, String.format("Bearer token null.  Id %s, Acct %s.",
                        mApplicationSession.getSessionId(), mApplicationSession.getAccountName()));
            }
        }
    }

}