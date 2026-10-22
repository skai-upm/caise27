package es.upm.etsisi.park.business.exception;

/**
 * The supplied password does not match the stored one.
 */
public class AuthenticationException extends ParkException {

    public AuthenticationException(String message) {
        super(message);
    }
}
