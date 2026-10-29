package es.upm.etsisi.amusementpark.business.exception;

/**
 * Raised when the supplied credentials do not match.
 */
public class AuthenticationException extends ParkException {

    public AuthenticationException(String message) {
        super(message);
    }
}
