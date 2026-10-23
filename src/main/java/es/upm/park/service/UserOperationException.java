package es.upm.park.service;

/**
 * Raised when a user operation is rejected. The message is shown to the
 * person issuing the command.
 */
public class UserOperationException extends Exception {

    public UserOperationException(String message) {
        super(message);
    }
}
