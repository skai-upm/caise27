package es.upm.etsisi.amusementpark;

/**
 * A park operation was rejected; the message explains why. No state was changed.
 */
public class OperationException extends Exception {
    public OperationException(String message) {
        super(message);
    }
}
