package es.upm.amusementpark.persistence.storage;

/**
 * Persistent data could not be read or written.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
