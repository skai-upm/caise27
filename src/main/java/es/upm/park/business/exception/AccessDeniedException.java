package es.upm.park.business.exception;

/**
 * An attraction refuses an entry (ticket type, capacity, passes).
 */
public class AccessDeniedException extends ParkException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
