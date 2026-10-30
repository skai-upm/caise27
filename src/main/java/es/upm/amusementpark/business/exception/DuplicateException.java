package es.upm.amusementpark.business.exception;

/**
 * An identifier, DNI, email or membership already exists.
 */
public class DuplicateException extends ParkException {

    public DuplicateException(String message) {
        super(message);
    }
}
