package es.upm.parque;

/**
 * A user operation was rejected. The message is meant to be shown to the user.
 */
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }
}
