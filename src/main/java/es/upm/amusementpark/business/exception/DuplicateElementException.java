package es.upm.amusementpark.business.exception;

/** An element with the same identity already exists or is already present. */
public class DuplicateElementException extends ParkException {

    public DuplicateElementException(String message) {
        super(message);
    }

    public DuplicateElementException(String message, Throwable cause) {
        super(message, cause);
    }
}
