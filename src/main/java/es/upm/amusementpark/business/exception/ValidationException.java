package es.upm.amusementpark.business.exception;

/**
 * A value does not satisfy the required format or business validation rules.
 */
public class ValidationException extends ParkException {

    public ValidationException(String message) {
        super(message);
    }
}
