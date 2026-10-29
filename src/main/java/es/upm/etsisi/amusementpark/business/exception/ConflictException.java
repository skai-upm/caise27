package es.upm.etsisi.amusementpark.business.exception;

/**
 * Raised when an identifier or unique value is already in use.
 */
public class ConflictException extends ParkException {

    public ConflictException(String message) {
        super(message);
    }
}
