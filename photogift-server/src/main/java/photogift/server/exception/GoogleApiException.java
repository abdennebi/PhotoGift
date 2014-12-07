package photogift.server.exception;

/**
 * Exception thrown when errors occurs querying the Google + API.
 */
public class GoogleApiException extends RuntimeException {
    public GoogleApiException(String message) {
        super(message);
    }
}