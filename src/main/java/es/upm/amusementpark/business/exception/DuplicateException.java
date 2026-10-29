package es.upm.amusementpark.business.exception;

/**
 * An identifier or unique value is already in use.
 */
public class DuplicateException extends ParkException {

    public DuplicateException(String message) {
        super(message);
    }

    public DuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
}
