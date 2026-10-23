package es.upm.park.business.exception;

/**
 * The supplied credentials do not identify a user.
 */
public class AuthenticationException extends ParkException {

    public AuthenticationException(String message) {
        super(message);
    }
}
