package es.upm.etsisi.amusementpark.business.exception;

/**
 * Input data does not satisfy a format or validation rule.
 */
public class ValidationException extends ParkException {

    public ValidationException(String message) {
        super(message);
    }
}
