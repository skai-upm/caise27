package es.upm.etsisi.amusementpark.user;

/**
 * A user operation was rejected; the message explains why. No state was changed.
 */
public class UserOperationException extends Exception {
    public UserOperationException(String message) {
        super(message);
    }
}
