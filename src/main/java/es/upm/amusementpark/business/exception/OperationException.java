package es.upm.amusementpark.business.exception;

/**
 * Base of the exceptions signalling that a requested park operation cannot be completed.
 * The message explains why. Handled by the presentation layer as a rejected command.
 */
public abstract class OperationException extends RuntimeException {

    protected OperationException(String message) {
        super(message);
    }
}
