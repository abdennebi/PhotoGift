package com.abdennebi.photogift.api.retrofit;

import android.support.annotation.Nullable;
import android.util.Log;

import com.abdennebi.photogift.api.ApiCallbacks;
import com.abdennebi.photogift.api.ApiClient;
import com.abdennebi.photogift.application.ApplicationSession;
import com.abdennebi.photogift.application.Listener;
import com.abdennebi.photogift.config.Constants;
import com.abdennebi.photogift.domain.Gift;
import com.abdennebi.photogift.domain.GiftChain;
import com.abdennebi.photogift.domain.TopGiver;
import com.abdennebi.photogift.domain.UploadUrl;
import com.abdennebi.photogift.domain.User;
import com.abdennebi.photogift.utils.Callback;
import com.abdennebi.photogift.utils.GsonUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

import static com.abdennebi.photogift.config.Constants.Headers.COOKIE_PREFIX;
import static com.abdennebi.photogift.config.Constants.Headers.HEADER_SETCOOKIE;
import static com.abdennebi.photogift.config.Constants.OAuth2.SERVER_URL;
import static com.abdennebi.photogift.utils.CallableAsyncTask.invoke;

public class ApiClientImpl implements ApiClient {

    public final ApplicationSession applicationSession;

    private final String TAG = "PhotoGift-ApiClientImpl";

    public ApiClientImpl(ApplicationSession applicationSession) {
        this.applicationSession = applicationSession;
    }

    private static String getHeader(String name, List<Header> headers) {
        for (Header header : headers) {
            if (name.equals(header.getName())) {
                return header.getValue();
            }
        }
        return null;
    }

    @Override
    public void fetchCurrentUser(final ApiCallbacks.ServiceCallback serviceCallback) {
        invoke
                (new Callable<User>() {
                     @Override
                     public User call() throws RetrofitError {
                         // 1 - Fetch the client
                         return api().fetchCurrentUser();

                     }
                 }, new Callback<User>() {
                     @Override
                     public void success(User user) {

                         // 2 - Call the callback function
                         serviceCallback.onUserRetrieved(user);
                     }

                     @Override
                     public void error(Exception e) {
                         // 3 - Process the error
                         if (e instanceof RetrofitError) {
                             Log.d(TAG, "Retrieve user error");
                             if (serviceCallback != null) {
                                 deliverError((RetrofitError) e, serviceCallback);
                             }
                         } else {
                             Log.e(TAG, "Error while retrieving user", e);
                         }
                     }
                 }
                );
    }

    @Override
    public void createGift(final @Nullable Long giftChainId, final String imageUri, final String title, final String text, final ApiCallbacks.Callback<Gift> callback) {

        invoke
                (new Callable<Gift>() {
                     @Override
                     public Gift call() throws RetrofitError {

                         Gift gift = null;
                         // 1 get Upload URL
                         // The upload URL is in the following format : http://photo-gift.appspot.com/_ah/upload/123456
                         UploadUrl uplaodUrl = api().getUplaodUrl();

                         String url = uplaodUrl.url;
                         // We extract the id
                         int i = url.lastIndexOf("_ah/upload/");

                         String uploadId = null;

                         if (i > 0) {
                             uploadId = url.substring(i + "_ah/upload/".length());
                         }

                         if (uploadId != null) {

                             TypedFile photo = new TypedFile("image/jpeg", new File(imageUri));

                             if (giftChainId != null) {
                                 gift = api().createGiftAndAddToGiftChain(uploadId, photo, new TypedString(title),
                                         new TypedString(text), new TypedString(giftChainId.toString()));
                             } else {
                                 gift = api().createGift(uploadId, photo, new TypedString(title), new TypedString(text));

                             }
                         }
                         return gift;
                     }
                 }, new Callback<Gift>() {
                     @Override
                     public void success(Gift result) {

                         // 2 - Call the callback function
                         callback.onSuccess(result);
                     }

                     @Override
                     public void error(Exception e) {
                         // 3 - Process the error
                         Log.e(TAG, "Error while retrieving GiftChain list", e);
                         callback.onFailure(e);
                     }
                 }
                );


        // 2 create gift
    }

    @Override
    public void setTouched(final @Nullable Long gifId, final ApiCallbacks.Callback<Gift> callback) {

        invoke
                (new Callable<Gift>() {
                     @Override
                     public Gift call() throws RetrofitError {
                         // 1 - Fetch the client
                         Api api = new RestAdapter.Builder()
                                 .setEndpoint(SERVER_URL)
                                 .setConverter(GsonUtils.getGsonConverter())
                                 .setLogLevel(RestAdapter.LogLevel.FULL)
                                 .setRequestInterceptor(new OAuthHandler(applicationSession))
                                 .build().create(Api.class);

                         return api().setTouched(gifId, true);

                     }
                 }, new Callback<Gift>() {
                     @Override
                     public void success(Gift result) {
                         callback.onSuccess(result);
                     }

                     @Override
                     public void error(Exception e) {
                         callback.onFailure(e);
                     }
                 }
                );
    }

    @Override
    public void getTopGivers(final ApiCallbacks.Callback<List<TopGiver>> callback) {
        invoke
                (new Callable<List<TopGiver>>() {
                     @Override
                     public List<TopGiver> call() throws RetrofitError {
                         // 1 - Fetch the client
//                         Api api = new RestAdapter.Builder()
//                                 .setEndpoint(SERVER_URL)
//                                 .setConverter(GsonUtils.getGsonConverter())
//                                 .setLogLevel(RestAdapter.LogLevel.FULL)
//                                 .setRequestInterceptor(new OAuthHandler(applicationSession))
//                                 .build().create(Api.class);

                         return api().getTopGivers();

                     }
                 }, new Callback<List<TopGiver>>() {
                     @Override
                     public void success(List<TopGiver> result) {
                         callback.onSuccess(result);
                     }

                     @Override
                     public void error(Exception e) {
                         callback.onFailure(e);
                     }
                 }
                );
    }

    @Override
    public void updateDoNotShowInapproriate(final boolean doNotShowInapproriate, final ApiCallbacks.Callback<User> callback) {
        invoke
                (new Callable<User>() {
                     @Override
                     public User call() throws RetrofitError {
                         return api().updateDoNotShowInappropriateContent(doNotShowInapproriate);

                     }
                 }, new Callback<User>() {
                     @Override
                     public void success(User result) {
                         callback.onSuccess(result);
                     }

                     @Override
                     public void error(Exception e) {
                         callback.onFailure(e);
                     }
                 }
                );
    }

    @Override
    public void signalInappropiate(final Long giftId, final ApiCallbacks.Callback<Gift> callback) {
        invoke
                (new Callable<Gift>() {
                     @Override
                     public Gift call() throws RetrofitError {
                         return api().setInappropriate(giftId, true);

                     }
                 }, new Callback<Gift>() {
                     @Override
                     public void success(Gift result) {
                         callback.onSuccess(result);
                     }

                     @Override
                     public void error(Exception e) {
                         callback.onFailure(e);
                     }
                 }
                );
    }

    @Override
    public void getGiftChainList(final Listener.GiftChainListListener listener) {

        invoke
                (new Callable<List<GiftChain>>() {
                     @Override
                     public List<GiftChain> call() throws RetrofitError {
                         // 1 - Fetch the client
                         Api api = new RestAdapter.Builder()
                                 .setEndpoint(SERVER_URL)
                                 .setConverter(GsonUtils.getGsonConverter())
                                 .setLogLevel(RestAdapter.LogLevel.FULL)
                                 .setRequestInterceptor(new OAuthHandler(applicationSession))
                                 .build().create(Api.class);

                         return api.getGiftChainList();

                     }
                 }, new Callback<List<GiftChain>>() {
                     @Override
                     public void success(List<GiftChain> result) {

                         // 2 - Call the callback function
                         listener.onReceived(result);
                     }

                     @Override
                     public void error(Exception e) {
                         // 3 - Process the error
                         Log.e(TAG, "Error while retrieving GiftChain list", e);
                     }
                 }
                );
    }

    @Override
    public void getGiftChain(final Long giftChainId, final Listener.GiftChainListener listener) {

        invoke
                (new Callable<GiftChain>() {
                     @Override
                     public GiftChain call() throws RetrofitError {

                         return api().getGiftChain(giftChainId);

                     }
                 }, new Callback<GiftChain>() {
                     @Override
                     public void success(GiftChain result) {

                         // 2 - Call the callback function
                         listener.onReceived(result);
                     }

                     @Override
                     public void error(Exception e) {
                         // 3 - Process the error
                         Log.e(TAG, "Error while retrieving GiftChain list", e);
                     }
                 }
                );
    }

    private Api api() {
        return new RestAdapter.Builder()
                .setEndpoint(SERVER_URL)
                .setConverter(GsonUtils.getGsonConverter())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(new OAuthHandler(applicationSession))
                .build().create(Api.class);
    }

    private void deliverError(RetrofitError error, ApiCallbacks.ServiceCallback serviceCallback) {
        Response response = error.getResponse();

        if (response != null) {
            List<Header> headers = response.getHeaders();

            String sessionCookie = extractCookieIfPresent(response, headers);
            if (sessionCookie != null) {
                applicationSession.storeSessionId(sessionCookie);
            }

            // The OAuth 2 Authorozation Code is required
            if (response.getStatus() == 401) {
                String codeError = getHeader(Constants.Headers.HEADER_XOAUTH, headers);
                if (codeError != null) {
                    serviceCallback.codeSignInRequired();
                } else {
                    String idTokenErr = getHeader(Constants.Headers.HEADER_WWWAUTH, headers);

                    if (idTokenErr != null) {
                        if (applicationSession != null) {
                            // We will blank out the session ID so we send an ID token.
                            applicationSession.storeSessionId(null);


                        }
                    }
                }
            }
        }
    }

    private String extractCookieIfPresent(Response response, List<Header> headers) {
        String cookie = getHeader(HEADER_SETCOOKIE, headers);
        String sessionCookie = null;
        if (cookie != null && cookie.startsWith(COOKIE_PREFIX)) {
            String session = cookie.substring(COOKIE_PREFIX.length());
            String[] sections = session.split(";");
            sessionCookie = sections[0];
        }
        return sessionCookie;
    }
}
