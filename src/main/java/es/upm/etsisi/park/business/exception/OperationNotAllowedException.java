package es.upm.etsisi.park.business.exception;

/** The operation breaks a business rule in the current park state (locks, dates, access, capacity). */
public class OperationNotAllowedException extends ParkException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
