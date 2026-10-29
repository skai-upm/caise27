package es.upm.amusementpark.business.exception;

/**
 * Persistent data could not be read or written.
 */
public class PersistenceException extends ParkException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
