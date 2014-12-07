package photogift.server.exception;

/**
 * Thrown if the current user's access token is expired and the user has no
 * refresh token stored.
 */
public class GoogleTokenExpirationException extends RuntimeException {
}