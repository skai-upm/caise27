package es.upm.etsisi.amusementpark.business.exception;

/**
 * The supplied credentials do not authenticate the user.
 */
public class AuthenticationException extends ParkException {

    public AuthenticationException(String message) {
        super(message);
    }
}
