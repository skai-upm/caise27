package es.upm.etsisi.amusementpark.business.exception;

/**
 * The supplied password does not match the user's credentials.
 */
public class AuthenticationException extends ParkException {

    public AuthenticationException(String message) {
        super(message);
    }
}
