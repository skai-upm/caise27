package es.upm.etsisi.park.service;

/**
 * A user operation was rejected; the message is meant to be shown to the user.
 */
public class UserOperationException extends RuntimeException {

    public UserOperationException(String message) {
        super(message);
    }
}
