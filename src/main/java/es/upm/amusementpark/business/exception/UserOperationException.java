package es.upm.amusementpark.business.exception;

/**
 * Signals that a user operation was rejected; the message explains why.
 */
public class UserOperationException extends ParkException {

    public UserOperationException(String message) {
        super(message);
    }
}
