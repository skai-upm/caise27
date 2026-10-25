package es.upm.etsisi.park.service;

/**
 * Signals that a user operation was rejected; the message is shown to the user.
 */
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }
}
