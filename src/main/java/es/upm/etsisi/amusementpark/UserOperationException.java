package es.upm.etsisi.amusementpark;

/**
 * Signals that a user operation was rejected; the message explains why.
 */
public class UserOperationException extends Exception {

    public UserOperationException(String message) {
        super(message);
    }
}
