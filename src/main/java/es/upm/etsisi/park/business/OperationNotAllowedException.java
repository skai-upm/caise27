package es.upm.etsisi.park.business;

/**
 * The operation is well formed but a business rule forbids it (locks, dates,
 * capacity, attraction type, passes, credentials).
 */
public class OperationNotAllowedException extends ParkException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
