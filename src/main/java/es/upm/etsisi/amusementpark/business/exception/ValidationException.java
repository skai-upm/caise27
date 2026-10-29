package es.upm.etsisi.amusementpark.business.exception;

/**
 * Raised when an input value does not satisfy the required format or rules.
 */
public class ValidationException extends ParkException {

    public ValidationException(String message) {
        super(message);
    }
}
