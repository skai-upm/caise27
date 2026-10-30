package es.upm.etsisi.amusementpark.business.exception;

/**
 * The operation is valid but forbidden by the current park state (locks, dates, capacity, attraction type, pass state).
 */
public class OperationNotAllowedException extends ParkException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
