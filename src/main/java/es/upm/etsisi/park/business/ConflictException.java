package es.upm.etsisi.park.business;

/**
 * Signals that an identifier or membership is already in use.
 */
public class ConflictException extends ParkException {

    public ConflictException(String message) {
        super(message);
    }
}
