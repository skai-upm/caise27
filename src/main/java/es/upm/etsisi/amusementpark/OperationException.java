package es.upm.etsisi.amusementpark;

/**
 * Signals that a park operation was rejected; the message explains why.
 */
public class OperationException extends Exception {

    public OperationException(String message) {
        super(message);
    }
}
