package photogift.server.domain;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Cache
public class User implements Serializable {

    public static String kind = "photogift#user";

    public static Key<User> key(long id) {
        return Key.create(User.class, id);
    }

    /**
     * Primary identifier of this User.
     */
    @Id
    public Long id;

    /**
     * Primary email address of this User.
     */
    @Index
    public String email;

    /**
     * UUID identifier of this User within Google products.
     */
    @Index
    public String googleUserId;

    /**
     * Display name that this User has chosen for Google products.
     */
    @Index
    public String googleDisplayName;

    /**
     * Public Google+ profile URL for this User.
     */
    public String googlePublicProfileUrl;

    /**
     * Public Google+ profile image for this User.
     */
    public String googlePublicProfilePhotoUrl;

    /**
     * Access token used to access Google APIs on this User's behalf.
     */
    @Index
    public String googleAccessToken;

    /**
     * Refresh token used to refresh this User's googleAccessToken.
     */
    public String googleRefreshToken;

    /**
     * Validity of this User's googleAccessToken in seconds.
     */
    public Long googleExpiresIn;

    /**
     * Expiration time in milliseconds since Epoch for this User's
     * googleAccessToken.
     * Exposed for mobile clients, to help determine if they should request a new
     * token.
     */
    public Long googleExpiresAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!id.equals(user.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns true if the cached Google user data is less than one day old
     */
    public boolean isDataFresh() {
        if (lastUpdated == null) {
            return false;
        }

        Date now = new Date();
        long timeDifference = now.getTime() - lastUpdated.getTime();
        return timeDifference < ONE_DAY_IN_MS;
    }

    /**
     * Used to determine whether the User's cached Google data is "fresh" (less than one day old).
     * <p/>
     * Note: You might prefer an alternate library for managing time in your application. We chose
     * Date for brevity in the sample.
     */
    public Date lastUpdated = null;

    /**
     * 1 day in milliseconds for cached data calculations (1000 * 60 * 60 * 24).
     * <p/>
     * Note: This is not a recommended way to manage time comparisons. However, we are using it for
     * brevity in the sample.
     */
    private static final Long ONE_DAY_IN_MS = 86400000L;

    public String googlePhotoUrl;

    /**
     * Public Google+ profile URL for this User.
     */
    public String googleProfileUrl;

    public void setLastUpdated() {
        lastUpdated = new Date();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", googleUserId='" + googleUserId + '\'' +
                ", googleDisplayName='" + googleDisplayName + '\'' +
                ", googlePublicProfileUrl='" + googlePublicProfileUrl + '\'' +
                ", googlePublicProfilePhotoUrl='" + googlePublicProfilePhotoUrl + '\'' +
                ", googleAccessToken='" + googleAccessToken + '\'' +
                ", googleRefreshToken='" + googleRefreshToken + '\'' +
                ", googleExpiresIn=" + googleExpiresIn +
                ", googleExpiresAt=" + googleExpiresAt +
                ", lastUpdated=" + lastUpdated +
                ", googlePhotoUrl='" + googlePhotoUrl + '\'' +
                ", googleProfileUrl='" + googleProfileUrl + '\'' +
                '}';
    }

    public boolean doNotShowInappropriate;

    public List<Long> touchingGifts;
}
