package es.upm.amusementpark.business.exception;

/**
 * The supplied credentials do not match.
 */
public class AuthenticationException extends ParkException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
