package es.upm.amusementpark.service;

/**
 * Signals that a user operation was rejected. The message explains why.
 */
public class UserOperationException extends OperationException {

    public UserOperationException(String message) {
        super(message);
    }
}
