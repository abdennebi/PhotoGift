package photogift.server.exception;

public class BadParameterException extends RuntimeException {

	public BadParameterException(String message) {
		super(message);
	}
	
	public BadParameterException(String message, Exception cause) {
	    super(message, cause);
	}
}
