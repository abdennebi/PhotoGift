package photogift.server.filter;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.repackaged.com.google.common.annotations.VisibleForTesting;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.gson.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import photogift.server.domain.User;
import photogift.server.exception.CredentialNotFoundException;
import photogift.server.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides the logic for authenticating a user before continuing with the intended API call.
 *
 * @author joannasmith@google.com (Joanna Smith)
 * @author ldenison@google.com (Lee Denison)
 */


@Component
public class Authenticate {

    /**
     * Key name in the session referring to the Google user ID of the current
     * user.
     */
    public static final String CURRENT_USER_SESSION_KEY = "me";

    /**
     * Session ID cookie name.
     */
    static final String SESSION_ID_NAME = "HaikuSessionId";

//    /* TODO : déplacer dans le Datastore */
//    /**
//     * Local storage of sessions and the user associated with each session. This unbounded mapping
//     * lives here, rather than the datastore, for simplicity in the sample as it is used in
//     * authenticating a user. For a production app, you would want a more reliable and
//     * maintainable storage solution.
//     */
//    static Map<String, User> authenticatedSessions = new ConcurrentHashMap<String, User>();

    /**
     * Special keyword for making Google API calls on behalf of the authenticated user.
     */
    private static final String ME = "me";

    /**
     * Special keyword for making Google API calls for the visible collection.GoogleClientSecrets
     */
    private static final String VISIBLE = "visible";

    /**
     * Client secret configuration.  This is read from the client_secrets.json file.
     */
//    @Autowired
    private GoogleClientSecrets clientSecrets;

    /**
     * This is the default redirect URI for web applications and tells the browser to return
     * to the parent of the dialog. This value is used to validate a redirect URI provided in
     * the X-Oauth-Code header. It is important to validate all supplied redirect URIs, as
     * explained in the OAuth specification (http://tools.ietf.org/html/rfc6749#section-10.6).
     */
    private static final String WEB_REDIRECT_URI = "postmessage";

    /**
     * This is the default redirect URI for installed applications and is the equivalent of
     * a "null" value. This value is used to validate a redirect URI provided in
     * the X-Oauth-Code header. It is important to validate all supplied redirect URIs, as
     * explained in the OAuth specification (http://tools.ietf.org/html/rfc6749#section-10.6).
     */
    private static final String INSTALLED_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    /**
     * Authentication identifier for an ID token.
     */
    private static final String BEARER_SCHEME = "Bearer";

//  /**
//   * Authentication identifier for an authorization code.
//   */
//  private static final String CODE_SCHEME = "X-OAuth-Code";

    /**
     * Name of the authorization code authorization header.
     */
    private static final String CODE_AUTHORIZATION_HEADER_NAME = "X-OAuth-Code";

    /**
     * Regular expression for identifying an authorization code header.
     */
    private static final Pattern CODE_REGEX =
            Pattern.compile("\\s*(\\S+)(?:\\s+redirect_uri='(.+)')?");

    /**
     * Name of the Bearer authorization header.
     */
    private static final String BEARER_AUTHORIZATION_HEADER_NAME = "Authorization";

    /**
     * Regular expression for identifying an authorization ID token header.
     */
    private static final Pattern BEARER_REGEX =
            Pattern.compile("\\s*" + BEARER_SCHEME + "\\s+(\\S+)");

    /**
     * For use in verifying access tokens, as the client library currently does not provide
     * a validation method for access tokens.
     */
    private static final String TOKEN_INFO_ENDPOINT =
            "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=%s";

    /**
     * Regular expression for identifying client IDs from the same API Console project.
     */
    private static final Pattern CLIENT_ID_REGEX = Pattern.compile("(^\\d+).*");

    /**
     * For use in building authentication response headers.
     */
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    /**
     * For use in building authentication response headers.
     */
    private static final String GOOGLE_REALM =
            "realm=\"https://www.google.com/accounts/AuthSubRequest\"";

    /**
     * Verifier object for use in validating ID tokens.
     */
    private final GoogleIdTokenVerifier idTokenVerifier;

    /**
     * Repository pattern to wrap a static/final call for testing purposes.
     */
    private final GoogleIdTokenRepository tokenRepo;

    /**
     * Logger for the Authenticate class.
     */
    Logger logger = Logger.getLogger("Authenticate");

    /**
     * HttpTransport to use for external requests.
     */
    private final HttpTransport httpTransport ;

    private final JsonFactory jsonFactory;

//    @Autowired
    private UserService userService;

    @Autowired
    Authenticate( HttpTransport httpTransport, JsonFactory jsonFactory, GoogleClientSecrets clientSecrets, UserService userService) {
        this.jsonFactory = jsonFactory;
        this.httpTransport = httpTransport;
        this.clientSecrets = clientSecrets;
        this.userService = userService;
        idTokenVerifier = new GoogleIdTokenVerifier(httpTransport, jsonFactory);
        tokenRepo =  new GoogleIdTokenRepository();
    }

    /**
     * This is the Client ID that you generated in the API Console.  It is stored
     * in the client secret JSON file.
     * The clientSecrets value is initialized at construction time, and is never null.
     */
    public String getClientId() {
        return clientSecrets.getWeb().getClientId();
    }

    /**
     * This is the Client secret that you generated in the API Console.  It is stored
     * in the client secret JSON file.
     * The clientSecrets value is initialized at construction time, and is never null.
     */
    public String getClientSecret() {
        return clientSecrets.getWeb().getClientSecret();
    }

    /**
     * Authenticates a user by associating this session with a user based on a provided ID token
     * {@code idAuth} and by associating a user with valid credentials based on a provided
     * authorization code {@code codeAuth}. Once a request has been authenticated, calls
     * chain.doFilter() to invoke the associated servlet and complete the API call made by
     * the client.
     *
     * @return the User object indicating the authenticated and authorized Haiku+ user, or null
     * to indicate that additional authentication information is needed.
     */
    public User requireAuthentication(String sessionId, HttpServletRequest request, HttpServletResponse response) {

        User user = null;
        String googleUserId = null;
        GoogleCredential googleCredential = null;

        // Isolate the authorization piece of the relevant headers, if they exist.
        String authCode = parseAuthorizationHeader(request, CODE_AUTHORIZATION_HEADER_NAME, CODE_REGEX, 1);
        String idTokenString = parseAuthorizationHeader(request, BEARER_AUTHORIZATION_HEADER_NAME, BEARER_REGEX, 1);
        String redirectUri = parseAuthorizationHeader(request, CODE_AUTHORIZATION_HEADER_NAME, CODE_REGEX, 2);

        // Must be one of these two values, correct to INSTALLED otherwise.
        if (!WEB_REDIRECT_URI.equals(redirectUri)) {
            redirectUri = INSTALLED_REDIRECT_URI;
        }

        if (authCode != null) {

            logger.fine("User supplied Authorization Code");
            logger.finer("User supplied Authorization Code : " + authCode);

            // The request supplied an authorization header, so we process the credentials before
            // attempting to service the request. If the credentials are valid, googleUserId will
            // be assigned the Google ID of the authorized user, and the credentials will be
            // stored in the DataStore.
            GoogleTokenResponse tokenResponse = exchangeAuthorizationCode(authCode, redirectUri, response);
            if (tokenResponse != null) {
                googleCredential = createCredential(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());

                // An ID Token is a cryptographically-signed JSON object encoded in base 64.
                // Normally, it is critical that you validate an ID Token before you use it,
                // but since you are communicating directly with Google over an
                // intermediary-free HTTPS channel and using your Client Secret to
                // authenticate yourself to Google, you can be confident that the token you
                // receive really comes from Google and is valid. If your server passes the
                // ID Token to other components of your app, it is extremely important that
                // the other components validate the token before using it.
                googleUserId = parseIdToken(tokenResponse, response);

                if (googleUserId != null) {
                    userService.updateCredentialWithGoogleId(googleUserId, googleCredential, tokenResponse);
                }
            }
        }

        if (idTokenString != null) {
            // The request supplied a bearer token, so we verify the token before attempting to
            // service the request. The bearer token may be either an ID token or an access token for
            // requests from iOS devices, which currently cannot use authorization codes.
            // If the verification succeeds, googleUserId will be assigned the Google ID of the
            // authenticated user.
            // If more than one authentication header is provided, and this verification fails, the
            // authorization credentials will be stored above, but googleUserId will be reset to null
            // to indicate that the currently signed in user is not authenticated.
            googleUserId = verifyBearerToken(idTokenString, response);
        }

        if (googleUserId != null) {
            // If a valid authentication header was supplied, or if valid credentials were associated
            // with this user, then we check to see if that same user is associated with the current
            // session before attempting to service the request. If not, a new session is created
            // and associated with the authenticated/authorized user.
            user = authenticateSession(googleUserId, request);
        }

        if (user == null) {
            // If the authentication or authorization step failed, then we were unable to retrieve
            // the associated Haiku+ user, so we check to see if there is a user associated with the
            // current session and attempt to service the request, based off of stored user credentials.
//            User sessionUser = authenticatedSessions.get(sessionId);
            User sessionUser = getUserFromSession(request);

            if (sessionUser == null) {
                // The HTTP session does not have an authenticated user ID in it (or we were unable
                // to find the corresponding user in our database) and the request did not supply
                // an authorization header, so we return a request to authenticate with a bearer
                // token.

                // IETF RFC6750 defines how we should indicate that the user agent needs to
                // authenticate with a bearer token.
                logger.log(Level.INFO, "The session does not have an authenticated user,"
                        + " so return a 401 and request a bearer token.");
                response.addHeader(WWW_AUTHENTICATE, BEARER_SCHEME + " " + GOOGLE_REALM);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                return null;

            } else {
                googleUserId = sessionUser.googleUserId;
            }
        }

        // At this point, we have the correct user associated with the current authenticated session,
        // so we attempt to service the request.
        try {
            // We check the profile on every call to ensure that the cached Google user data is fresh,
            // since the Date check is cheap.
            return updateUserCache(request, googleUserId);
        } catch (CredentialNotFoundException e) {
            logger.log(Level.INFO,
                    "No credentials for Google user:" + googleUserId + "; request auth code with 401");
            // There are no associated credentials for the user, so we return a request to
            // authenticate with an authorization code.

            // There is no standard way for our server to request a new authorization code from
            // our client, so we use an non-standard scheme (X-OAuth-Code) to indicate that we
            // need a new refresh token.
            response.addHeader(CODE_AUTHORIZATION_HEADER_NAME, GOOGLE_REALM);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        } catch (InvalidAccessTokenException e) {
            logger.log(Level.INFO,
                    "Access token expired for user" + googleUserId + "; request auth code with 401");
            // The refresh token was invalidated, so we return a request to authenticate with
            // an authorization code.

            // There is no standard way for our server to request a new authorization code from
            // our client, so we use an non-standard scheme (X-OAuth-Code) to indicate that we
            // need a new refresh token.
            response.addHeader(CODE_AUTHORIZATION_HEADER_NAME, GOOGLE_REALM);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        } catch (IOException e) {
            logger.log(Level.INFO, "Something went wrong processing the request; return 500", e);
            // Likely a temporary network error
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    public static User getUserFromSession(HttpServletRequest request) {
        User sessionUser = null;

        HttpSession session = request.getSession();
        Object userIdObj = session.getAttribute(CURRENT_USER_SESSION_KEY);

        if (userIdObj != null) {
            sessionUser =  (User) userIdObj;
        }

//        String devMode = request.getParameter("devMode");
//
//        if (sessionUser == null && devMode != null) {
//            // TODO : for developement only
//            sessionUser = new User();
//            sessionUser.id = 5629499534213120l;
//            sessionUser.googleDisplayName = "Abdennebi Mohamed";
//            sessionUser.googlePhotoUrl = "https://lh4.googleusercontent.com/-w3DJcWPYV0k/AAAAAAAAAAI/AAAAAAAALag/MdtntdMRCRE/photo.jpg?sz=50";
//        }

        return sessionUser;
    }

    /**
     * Exchanges an authorization code for Google credentials, including an access and a refresh
     * token. If the exchange fails, an IOException is raised, and a 400 is specified in the
     * HTTP response, indicating that the authorization code was invalid. If the exchange succeeds,
     * a GoogleTokenResponse object is returned. Otherwise, null is returned to indicate a failure.
     */
    private GoogleTokenResponse exchangeAuthorizationCode(String code, String redirectUri,
                                                          HttpServletResponse response) {
        try {
            // Upgrade the authorization code into an access and refresh token.
            return createTokenExchanger(code, redirectUri).execute();
        } catch (TokenResponseException e) {
            //Failed to exchange authorization code.
            logger.log(Level.INFO, "Failed to exchange auth code; return 400", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        } catch (IOException e) {
            logger.log(Level.INFO, "Failed to exchange auth code; return 400", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    /**
     * Parses an ID token from a TokenResponse. If the parse fails, a 500 is specified and
     * null is returned, indicating that the ID token Google returned with a Credential object
     * is invalid. Otherwise, the Google user ID associated with the token is returned.
     */
    private String parseIdToken(GoogleTokenResponse tokenResponse, HttpServletResponse response) {
        try {
            // You can read the Google user ID in the ID token.
            GoogleIdToken idToken = tokenResponse.parseIdToken();
            return getGoogleIdFromIdToken(idToken);
        } catch (IOException e) {
            logger.log(Level.INFO, "Failed to parse ID token from tokenResponse object; return 500", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    /**
     * Retrieves the Google ID of a user out of an ID token.
     */
    private String getGoogleIdFromIdToken(GoogleIdToken idToken) {
        Payload payload = idToken.getPayload();
        return payload.getSubject();
    }

    /**
     * Create a GoogleCredential from the provided access and refresh tokens.
     */
    @VisibleForTesting
    GoogleCredential createCredential(String accessToken, String refreshToken) {
        return new GoogleCredential.Builder()
                .setJsonFactory(jsonFactory)
                .setTransport(httpTransport)
                .setClientSecrets(clientSecrets)
                .addRefreshListener(new InvalidateRefreshTokenOnExpired())
                .build()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);
    }

    /**
     * Validates the provided bearer token as an ID token. If the validation fails, an attempt
     * is made to validate the token as an access token. If the verifier fails, a 403 is
     * specified in the HTTP response, whereas an IOException thrown by a Google server indicates
     * that the token was invalid and a 400 is specified in the response.
     *
     * @return Google ID of the user the token is associated with; null if the token is invalid.
     */
    private String verifyBearerToken(String idTokenString, HttpServletResponse response) {
        String googlePlusId;
        GoogleIdToken idToken;
        try {
            // Attempt to parse the bearer token as an ID token. If this fails, pass the bearer token
            // along to be parsed as an access token.
            idToken = tokenRepo.parse(jsonFactory, idTokenString);

        } catch (IllegalArgumentException e) {

            logger.log(Level.INFO, "Failed to verify bearer token as ID token; attempting to verify as access token");

            return verifyAccessToken(idTokenString, response);

        } catch (IOException e) {
            logger.log(Level.INFO, "Failed to parse ID token; return 400", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        try {
            // Verify the ID token before retrieving the Google ID of the user associated with the token
            if (idTokenVerifier.verify(idToken) && tokenRepo.verifyAudience(idToken, Collections.singletonList(getClientId()))) {
                googlePlusId = getGoogleIdFromIdToken(idToken);
                return googlePlusId;

            } else {
                return null;
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Failed to verify ID token", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        } catch (GeneralSecurityException e) {
            logger.log(Level.INFO, "Failed to verify ID token", e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    /**
     * Validates the provided bearer token against the OAuth TokenInfo endpoint. If the HTTP request
     * fails, a 500 is specified in the response. If the token is invalid, a 400 is specified.
     * If the token is valid, a credential object is created and stored against the Google user
     * ID and that ID is returned. Otherwise, null is returned to indicate the failure.
     */
    @VisibleForTesting
    String verifyAccessToken(String accessToken, HttpServletResponse response) {
        TokenInfoResponse tokenResponse = null;
        try {
            // Form a request to the token info endpoint, since the Java client library does not
            // provide a method to validate an access token.
            HttpResponse httpResponse = httpTransport.createRequestFactory()
                    .buildGetRequest(new GenericUrl(String.format(TOKEN_INFO_ENDPOINT, accessToken)))
                    .execute();

            // Read the response into a TokenInfoResponse object.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                getContent(httpResponse.getContent(), out);

                tokenResponse = tokenRepo.fromJson(out);
            } catch (JsonParseException e) {
                logger.log(Level.INFO, "Unable to parse token info HTTP response; return 400", e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            } finally {
                out.close();
            }
        } catch (HttpResponseException e) {
            logger.log(Level.INFO, "Token info HTTP request failed with error code", e);
            // The response code from the GET request was an error code.
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        } catch (IOException e) {
            logger.log(Level.INFO, "Response from token info HTTP request was malformed", e);
            // The response from the GET request was malformed or could not be read.
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

        String googlePlusId = null;
        // The client ID may not be a perfect match if the request came from an installed
        // application. Rather than verifying equality of the client IDs, we need to verify
        // that both client IDs belong to the same project. The easiest way to do this is to
        // compare the initial digit grouping from the client IDs, as this first part will
        // always be the project ID.
        Matcher tokenMatcher = CLIENT_ID_REGEX.matcher(tokenResponse.audience);
        Matcher clientIdMatcher = CLIENT_ID_REGEX.matcher(getClientId());
        boolean tokenMatch = tokenMatcher.matches();
        boolean clientIdMatch = clientIdMatcher.matches();
        String tokenProject = "";
        String clientIdProject = "";
        if (tokenMatch && clientIdMatch) {
            tokenProject = tokenMatcher.group(1);
            clientIdProject = clientIdMatcher.group(1);
        }

        if (tokenResponse.expiresIn != null && tokenProject.equals(clientIdProject)) {
            googlePlusId = tokenResponse.userId;
            userService.updateCredentialWithGoogleId(googlePlusId, createCredential(accessToken, null), null);
        }
        return googlePlusId;
    }

    /**
     * Checks if the current session is associated with the specified Google user. If so, the
     * user object is returned. If not, the current session is discarded and a new one is
     * created and associated with the user object of the specified Google user.
     */
    private User authenticateSession(String googleUserId, HttpServletRequest request) {

        // Verify correct association beteween GoogleUserId and the Session
        Object obj = request.getSession().getAttribute(CURRENT_USER_SESSION_KEY);
        User user;
        if (obj != null) {
            user = (User) obj;
            if (googleUserId.equals(user.googleUserId)) {
                // We have the correct user associated with the current session, so return the user object.
                return user;
            } else {
                // The user IDs don't match, so we disassociate the current session from the user.
                request.getSession().removeAttribute(CURRENT_USER_SESSION_KEY);
            }
        }

        user = userService.loadUserWithGoogleId(googleUserId);

        if (user == null) {
            // Create a new user
            user = new User();
            user.googleUserId = googleUserId;
            userService.save(user);
        }

        // Invalidate the current session and authenticate the user for the new session.
        if (!request.getSession().isNew()) {
            request.getSession().invalidate();
        }

        request.getSession().setAttribute(CURRENT_USER_SESSION_KEY, user);

        return user;
    }

    /**
     * @return the authorization component of the header associated with the provided name based
     * on the provided regex pattern; null if the component does not exist
     */
    private String parseAuthorizationHeader(HttpServletRequest request, String headerName,
                                            Pattern pattern, int groupNumber) {
        String header = request.getHeader(headerName);
        String auth = null;
        if (header != null) {
            Matcher authMatcher = pattern.matcher(header);
            boolean authMatch = authMatcher.matches();
            if (authMatch) {
                if (authMatcher.groupCount() >= groupNumber) {
                    auth = authMatcher.group(groupNumber);
                }
            }
        }

        return auth;
    }

    /**
     * Updates the user's Google data in the Haiku+ DataStore, if the cached data is more
     * than one day old.
     *
     * @param googleId the current authenticated user
     * @return the User object stored in the DataStore
     */
    private User updateUserCache(HttpServletRequest request, String googleId) throws CredentialNotFoundException, IOException {
//        User profile = userService.loadUserWithGoogleId(googleId);

        User profile = (User) request.getSession().getAttribute(CURRENT_USER_SESSION_KEY);

        if (profile == null) {
            profile = new User();
            profile.googleUserId = googleId;
        }

        // If the user's profile was updated less than one day ago, do nothing.
        if (!profile.isDataFresh()) {
            fetchGoogleUserData(profile);
            userService.save(profile);
        }

        return profile;
    }

    /**
     * Performs the Google+ people.get API call to refresh a user's cached Google data. This data
     * should never be stored permanently and should be refreshed regularly (i.e. if it is older
     * than 24 hours).
     *
     * @param user the current authenticated user
     */
    private void fetchGoogleUserData(User user) throws CredentialNotFoundException, IOException {
        GoogleCredential credential = userService.requireCredentialWithGoogleId(user.googleUserId);

        Plus service = createPlusApiClient(credential);
        Person person = service.people().get(ME).execute();

        // A Person resource contains many things, but in this sample, we chose to focus on the
        // Google ID, the display name, and the URLs of the user's profile and profile photo.
        user.googleUserId = person.getId();
        user.googleDisplayName = person.getDisplayName();
        user.googlePhotoUrl = person.getImage().getUrl();
        user.googleProfileUrl = person.getUrl();
        user.setLastUpdated();

        // Now we call a separate method to perform the people.list API call to retrieve the
        // user's circles via a background thread.
//        fetchGooglePeopleList(user, service);
    }

    /**
     * Reads the content of an InputStream.
     *
     * @param inputStream  the InputStream to be read.
     * @param outputStream the content of the InputStream as a ByteArrayOutputStream.
     */
    private static void getContent(InputStream inputStream, ByteArrayOutputStream outputStream)
            throws IOException {
        // Read the response into a buffered stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            int readChar;
            while ((readChar = reader.read()) != -1) {
                outputStream.write(readChar);
            }
        } finally {
            reader.close();
        }
    }

    /**
     * Creates a token verifier object using the client secret values provided by client_secrets.json.
     *
     * @param code        the authorization code to be exchanged for a bearer token once verified
     * @param redirectUri the redirect URI to be used for token exchange, from the APIs console.
     */
    @VisibleForTesting
    GoogleAuthorizationCodeTokenRequest createTokenExchanger(String code,
                                                             String redirectUri) {
        return new GoogleAuthorizationCodeTokenRequest(httpTransport,
                jsonFactory,
                getClientId(),
                getClientSecret(),
                code,
                redirectUri);
    }

    /**
     * Creates a new authorized Google+ API client that can make calls to the Google+ API on behalf
     * of the application through the Java client library.
     */
    @VisibleForTesting
    Plus createPlusApiClient(GoogleCredential credential) {
        return new Plus.Builder(httpTransport, jsonFactory, credential).build();
    }

    /**
     * Internal class used to monitor access and refresh tokens. If a token is invalid for an
     * API request, the tokens in the credential are inalidated and an InvalidAccessTokenException
     * is thrown to indicate that a new authorization code or bearer token is needed from the client.
     */
    public static class InvalidateRefreshTokenOnExpired implements CredentialRefreshListener {
        @Override
        public void onTokenErrorResponse(Credential credential, TokenErrorResponse error)
                throws IOException {
            if (error != null && "invalid_grant".equals(error.getError())) {
                credential.setAccessToken(null);
                credential.setRefreshToken(null);
                throw new InvalidAccessTokenException();
            }
        }

        @Override
        public void onTokenResponse(Credential credential, TokenResponse response) throws IOException {
        }
    }

    /**
     * Inner class to define the InvalidAccessTokenException, which is raised when a
     * refresh token fails to refresh the access token, indicating that the token has
     * expired or been invalidated and a new one is needed. Also may be raised when no
     * refresh token exists an the access token has expired, as is the case for the iOS client.
     */
    @VisibleForTesting
    static class InvalidAccessTokenException extends IOException {
    }


}
