package es.upm.etsisi.amusementpark.business;

/**
 * Input data is malformed or violates a validation rule.
 */
public class InvalidDataException extends ParkException {

    public InvalidDataException(String message) {
        super(message);
    }
}
