package es.upm.amusementpark.business.exception;

/**
 * Signals that a pass purchase or refund was rejected; the message explains
 * why.
 */
public class PassOperationException extends ParkException {

    public PassOperationException(String message) {
        super(message);
    }
}
