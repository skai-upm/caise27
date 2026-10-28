package es.upm.etsisi.amusementpark.business.exception;

/**
 * A value supplied to an operation has an invalid format or range.
 */
public class InvalidValueException extends ParkException {

    public InvalidValueException(String message) {
        super(message);
    }
}
