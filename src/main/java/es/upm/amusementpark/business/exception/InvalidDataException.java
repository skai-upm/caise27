package es.upm.amusementpark.business.exception;

/**
 * Supplied data violates a validation rule.
 */
public class InvalidDataException extends ParkException {

    public InvalidDataException(String message) {
        super(message);
    }
}
