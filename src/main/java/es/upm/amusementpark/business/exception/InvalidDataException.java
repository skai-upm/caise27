package es.upm.amusementpark.business.exception;

/**
 * Input data that breaks a validation rule (format, value range, password rules...).
 */
public class InvalidDataException extends ParkException {

    public InvalidDataException(String message) {
        super(message);
    }
}
