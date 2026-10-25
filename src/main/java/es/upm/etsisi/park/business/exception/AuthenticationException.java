package es.upm.etsisi.park.business.exception;

/**
 * The supplied credentials do not match a registered user.
 */
public class AuthenticationException extends ParkException {

    public AuthenticationException(String message) {
        super(message);
    }
}
