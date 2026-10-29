package es.upm.amusementpark.business.exception;

/**
 * Invalid input data (malformed values, failed validation rules).
 */
public class ValidationException extends ParkException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
