package es.upm.etsisi.park;

/**
 * A command was rejected; the message explains why to the user.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
