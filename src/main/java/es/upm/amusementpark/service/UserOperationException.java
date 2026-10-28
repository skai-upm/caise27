package es.upm.amusementpark.service;

/**
 * Signals that a user operation was rejected; the message explains why.
 */
public class UserOperationException extends Exception {

    private static final long serialVersionUID = 1L;

    public UserOperationException(String message) {
        super(message);
    }
}
