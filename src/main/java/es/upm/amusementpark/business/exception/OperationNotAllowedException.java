package es.upm.amusementpark.business.exception;

/**
 * The operation is not permitted in the current state (dates, locks, capacity, access rules).
 */
public class OperationNotAllowedException extends ParkException {

    public OperationNotAllowedException(String message) {
        super(message);
    }

    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
