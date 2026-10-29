package es.upm.amusementpark.business.exception;

/**
 * The operation is well formed but the current park state forbids it (dates, locks, capacity, passes).
 */
public class OperationNotAllowedException extends ParkException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
