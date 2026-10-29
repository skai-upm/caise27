package es.upm.amusementpark.business.exception;

/** A supplied value is malformed or outside its allowed range. */
public class InvalidValueException extends ParkException {

    public InvalidValueException(String message) {
        super(message);
    }

    public InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
