package es.upm.amusementpark.business.exception;

/**
 * The operation is not allowed in the current park state (dates, locks, capacity, credentials).
 */
public class OperationNotAllowedException extends ParkException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
