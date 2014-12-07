package com.abdennebi.photogift.api.retrofit;

import java.util.List;

import com.abdennebi.photogift.domain.Gift;
import com.abdennebi.photogift.domain.GiftChain;
import com.abdennebi.photogift.domain.TopGiver;
import com.abdennebi.photogift.domain.UploadUrl;
import com.abdennebi.photogift.domain.User;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

public interface Api {

    public static final String USER = "/api/users/me";

    public static final String GIFT_CHAIN_LIST = "/api/giftchains/pages";

    public static final String GIFT_CHAIN = "/api/giftchains";

    public static final String GIFT = "/api/gifts";

    public static final String GET_UPLAOD_URL = GIFT + "/uploadUrl";

    public static final String TOUCHED = GIFT + "/{id}/touched/{touched}";

    public static final String INAPPROPRIATE = GIFT + "/{id}/inappropriate/{inappropriate}";

    public static final String TOP_GIVERS = GIFT + "/topgivers";

    public static final String UPLAOD_URL_PREFIX = "/_ah/upload";

    /**
     * Fetch the connected User data.
     */
    @GET(USER)
    public User fetchCurrentUser();

    /**
     * Update the user Preference 'DoNotShowInappropriate Content'
     */
    @POST(USER)
    public User updateDoNotShowInappropriateContent(@Query("doNotShowInappropriate") boolean value);

    /**
     * Fecth the GiftChain list.
     */
    @GET(GIFT_CHAIN_LIST)
    public List<GiftChain> getGiftChainList();

    /**
     * Fetch a GiftChain by its Id.
     */
    @GET(GIFT_CHAIN + "/{id}")
    public GiftChain getGiftChain(@Path("id") Long giftChainId);

    /**
     * Fetch a Gift by its Id.
     */
    @GET(GIFT + "/{id}")
    public Gift getGift(@Path("id") Long giftId);

    /**
     * Get a Generated one time URL for uploading Image to the Google BlobStore.
     *
     * A generated URL has the following format :
     *
     * http://photo-gift.appspot.com/_ah/upload/{id}/
     */
    @GET(GET_UPLAOD_URL)
    public UploadUrl getUplaodUrl();

    /**
     * Create a Gift
     * @param uploadId The generated upload id retrieved by calling {@link #getUplaodUrl()}
     * @return the representation of the newly created Gift.
     */
    @Multipart
    @POST(UPLAOD_URL_PREFIX + "/{id}")
    Gift createGiftAndAddToGiftChain(@Path(value= "id", encode = false) String uploadId, @Part("photo") TypedFile photo,
                    @Part("title") TypedString title, @Part("text") TypedString text,
                    @Part("giftChainId") TypedString giftChainId);

    @Multipart
    @POST(UPLAOD_URL_PREFIX + "/{id}")
    Gift createGift(@Path(value= "id", encode = false)  String uploadId, @Part("photo") TypedFile photo,
                    @Part("title") TypedString title, @Part("text") TypedString text);


    /**
     * Set to touched flag if the user is touched. (in future the user can change his mind)
     * @param giftId The Gift Id
     * @param touched to say if user is touched or not.
     * @return The updated Gift.
     */
    @POST(TOUCHED)
    public Gift setTouched(@Path("id") Long giftId, @Path("touched") boolean touched);

    @POST(INAPPROPRIATE)
    public Gift setInappropriate(@Path("id") Long giftId, @Path("inappropriate") boolean inappropriate);

    @GET(TOP_GIVERS)
    public List<TopGiver> getTopGivers();



}