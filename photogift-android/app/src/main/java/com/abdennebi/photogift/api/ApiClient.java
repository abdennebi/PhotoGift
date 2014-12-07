package com.abdennebi.photogift.api;

import android.support.annotation.Nullable;

import com.abdennebi.photogift.application.Listener;
import com.abdennebi.photogift.domain.Gift;
import com.abdennebi.photogift.domain.GiftChain;
import com.abdennebi.photogift.domain.TopGiver;
import com.abdennebi.photogift.domain.User;

import java.util.List;

public interface ApiClient {

    /**
     * Get the list of all {@link GiftChain}s.
     */
    void getGiftChainList(Listener.GiftChainListListener listener);

    /**
     * Get the GiftChain identified by its Id.
     */
    void getGiftChain(Long giftChainId, Listener.GiftChainListener listener);

    /**
     * Fetches the current client, the result is to transmited to {@link ApiCallbacks.ServiceCallback}
     * @param serviceCallback
     */
    void fetchCurrentUser(final ApiCallbacks.ServiceCallback serviceCallback);

    void createGift(@Nullable Long giftChainId, String imageUri, String title, String text, ApiCallbacks.Callback<Gift> callback);

    void setTouched(@Nullable Long gifId, ApiCallbacks.Callback<Gift> callback);

    void getTopGivers( ApiCallbacks.Callback<List<TopGiver>> callback);

    void updateDoNotShowInapproriate(boolean doNotShowInapproriate, ApiCallbacks.Callback<User> callback);

    void signalInappropiate(Long giftId, ApiCallbacks.Callback<Gift> callback);
}
