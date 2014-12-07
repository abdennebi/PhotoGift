package photogift.server.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.googlecode.objectify.cmd.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import photogift.server.domain.TouchingGift;
import photogift.server.domain.User;
import photogift.server.exception.CredentialNotFoundException;
import photogift.server.exception.GoogleApiException;
import photogift.server.exception.GoogleTokenExpirationException;
import photogift.server.exception.UserNotFoundException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static photogift.server.config.OfyService.ofy;

@Service
public class UserService {

    @Autowired
    private HttpTransport httpTransport;

    @Autowired
    private JsonFactory jsonFactory;

    @Autowired
    private GoogleClientSecrets googleClientSecrets;

    /**
     * 100 seconds in milliseconds for token expiration calculations.
     */
    private static final Long HUNDRED_SECONDS_IN_MS = 100000l;

    /**
     * Either:
     * 1. Create a user for the given ID and credential
     * 2. or, update the existing user with the existing credential
     * <p/>
     * If 2, then ask Google for the user's public profile information to store.
     *
     * @param googleUserId Google user ID to update.
     * @param googleCredential        Credential to set for the user.
     * @return Updated User.
     * @throws GoogleApiException Could not fetch profile info for user.
     */
    public User updateCredentialWithGoogleId(String googleUserId, GoogleCredential googleCredential, GoogleTokenResponse googleTokenResponse) throws GoogleApiException {
        User user = ofy().load().type(User.class)
                .filter("googleUserId", googleUserId).first().now();

        // Register a new user.  Collect their Google profile info first.
        if (user == null) {
            System.out.println("Register a new user.  Collect their Google profile info first.");
            Plus plus = new Plus.Builder(httpTransport, jsonFactory, googleCredential).build();
            Person profile;
            Plus.People.Get get;
            try {
                get = plus.people().get("me");
                profile = get.execute();
            } catch (IOException e) {
                throw new GoogleApiException(e.getMessage());
            }
            user = new User();
            user.googleUserId = profile.getId();
            user.googleDisplayName = profile.getDisplayName();
            user.googlePublicProfileUrl = profile.getUrl();
            user.googlePublicProfilePhotoUrl = profile.getImage().getUrl();
            user.googleAccessToken = googleCredential.getAccessToken();
            user.googleAccessToken = googleCredential.getAccessToken();
            if (googleTokenResponse != null) user.googleExpiresAt =   googleTokenResponse.getExpiresInSeconds();
            System.out.println(user);
        }
        // TODO(silvano): Also fetch and set the email address for the user.
        user.googleAccessToken = googleCredential.getAccessToken();

        if (googleCredential.getRefreshToken() != null) {
            user.googleRefreshToken = googleCredential.getRefreshToken();
        }

//        user.googleExpiresAt = googleCredential.getExpirationTimeMilliseconds();
//        user.googleExpiresIn = googleCredential.getExpiresInSeconds();

        if (googleTokenResponse != null) {
            user.googleExpiresAt = googleTokenResponse.getExpiresInSeconds() * 1000;
            user.googleExpiresIn = googleTokenResponse.getExpiresInSeconds();
        }

        ofy().save().entity(user).now();
        return user;
    }

    public void save(User user) {
        ofy().save().entity(user).now();
    }

    public User loadUserWithGoogleId(String googleId) {
        User user = ofy().load().type(User.class)
                .filter("googleUserId", googleId).first().now();

        // Get user's touching gifts

        loadTouchingGifts(user);

        return user;
    }

    public User loadUser(Long id) {
        User user = ofy().load().type(User.class).id(id).now();

        loadTouchingGifts(user);

        return user;
    }

    private void loadTouchingGifts(User user) {
        Query<TouchingGift> query = ofy().load().type(TouchingGift.class).filter("touchedUserId", user.id);
        List<TouchingGift> touchingGifts = query.list();

        ArrayList<Long> giftIds = new ArrayList<Long>();

        for (TouchingGift touchingGift : touchingGifts) {
            giftIds.add(touchingGift.giftId);
        }

        user.touchingGifts = giftIds;
    }

    public GoogleCredential requireCredentialWithGoogleId(String googleUserId) throws GoogleTokenExpirationException, CredentialNotFoundException, IOException {

        // TODO get the user from the session
        User loggedInUser = ofy().load().type(User.class)
                .filter("googleUserId", googleUserId).first().now();
        System.out.println("googleUserId : " + googleUserId);
        System.out.println(loggedInUser);

        if (loggedInUser.googleAccessToken == null) {
            throw new CredentialNotFoundException();
        }

        // If the user doesn't have a refresh token, check if the expiration of the
        // access token is near to signal to the client to get a new token.
        if (loggedInUser.googleRefreshToken == null) {
            Long now = new Date().getTime();
            if (now >= (loggedInUser.googleExpiresAt - HUNDRED_SECONDS_IN_MS)) {
//                 TODO : revoir cette partie
//                throw new GoogleTokenExpirationException();
            }
        }
        System.out.println(loggedInUser);
//        System.out.println(clientSecretsResource);
        System.out.println(jsonFactory);
//        googleClientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(clientSecretsResource.getInputStream()));
        System.out.println(googleClientSecrets);
        System.out.println(googleClientSecrets.getWeb());


        GoogleCredential credential = new GoogleCredential.Builder()
                .setJsonFactory(jsonFactory)
                .setTransport(httpTransport)
                .setClientSecrets(googleClientSecrets.getWeb().getClientId(), googleClientSecrets.getWeb().getClientSecret()).build()
                .setAccessToken(loggedInUser.googleAccessToken)
                .setRefreshToken(loggedInUser.googleRefreshToken)
                .setExpirationTimeMilliseconds(loggedInUser.googleExpiresAt);

        return credential;
    }

    public User setDoNotShowInappropriate(User user, boolean doNotShowInappropriateValue) {
        User updatedUser = loadUser(user.id);
        updatedUser.doNotShowInappropriate = doNotShowInappropriateValue;
        save(updatedUser);
        return updatedUser;
    }
}
