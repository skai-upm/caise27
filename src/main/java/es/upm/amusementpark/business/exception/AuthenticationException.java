package es.upm.amusementpark.business.exception;

/**
 * The supplied credentials do not match the stored ones.
 */
public class AuthenticationException extends ParkException {

    public AuthenticationException(String message) {
        super(message);
    }
}
