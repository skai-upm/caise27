package es.upm.parque;

/**
 * A user operation was rejected; the message is shown to the person issuing the command.
 */
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }
}
