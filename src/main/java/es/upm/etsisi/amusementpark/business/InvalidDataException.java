package es.upm.etsisi.amusementpark.business;

/**
 * Thrown when a supplied value does not satisfy the validation rules.
 */
public class InvalidDataException extends ParkException {
    public InvalidDataException(String message) {
        super(message);
    }
}
