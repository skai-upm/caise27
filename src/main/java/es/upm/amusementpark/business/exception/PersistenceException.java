package es.upm.amusementpark.business.exception;

/**
 * Signals that persistent information could not be read or written.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
