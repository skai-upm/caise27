package es.upm.amusementpark.service;

/**
 * Signals that an operation was rejected; the message explains why.
 */
public class OperationException extends Exception {

    private static final long serialVersionUID = 1L;

    public OperationException(String message) {
        super(message);
    }
}
