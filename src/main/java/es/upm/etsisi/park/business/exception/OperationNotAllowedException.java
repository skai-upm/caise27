package es.upm.etsisi.park.business.exception;

/**
 * The operation is not allowed in the current park state (dates, locks, consumption).
 */
public class OperationNotAllowedException extends ParkException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
