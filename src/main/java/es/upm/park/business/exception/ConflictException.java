package es.upm.park.business.exception;

/**
 * An identifier or membership is already in use.
 */
public class ConflictException extends ParkException {

    public ConflictException(String message) {
        super(message);
    }
}
