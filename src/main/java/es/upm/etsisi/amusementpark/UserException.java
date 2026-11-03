package es.upm.etsisi.amusementpark;

/**
 * A user operation was rejected; the message explains why.
 */
public class UserException extends Exception {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
