package es.upm.amusementpark;

/**
 * Signals that an operation was rejected; the message explains why.
 */
public class OperationException extends Exception {

    public OperationException(String message) {
        super(message);
    }
}
