package es.upm.amusementpark.business.exception;

/**
 * An element would break a uniqueness rule (identifier, DNI, email, membership).
 */
public class DuplicateException extends ParkException {

    public DuplicateException(String message) {
        super(message);
    }
}
