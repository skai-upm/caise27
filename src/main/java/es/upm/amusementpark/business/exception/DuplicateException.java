package es.upm.amusementpark.business.exception;

/**
 * An identifier, DNI or email is already in use.
 */
public class DuplicateException extends ParkException {

    public DuplicateException(String message) {
        super(message);
    }
}
