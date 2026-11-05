package es.upm.amusementpark.service;

/**
 * Signals that a requested park operation was rejected. The message explains why.
 */
public class OperationException extends RuntimeException {

    public OperationException(String message) {
        super(message);
    }
}
