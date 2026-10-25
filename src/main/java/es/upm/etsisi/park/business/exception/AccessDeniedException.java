package es.upm.etsisi.park.business.exception;

/**
 * An attraction refused entry (ticket kind, date, capacity or passes).
 */
public class AccessDeniedException extends ParkException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
