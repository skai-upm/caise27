package es.upm.parque;

/**
 * Signals that a user operation was rejected; the message explains why.
 */
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }
}
