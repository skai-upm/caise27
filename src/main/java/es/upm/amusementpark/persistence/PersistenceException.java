package es.upm.amusementpark.persistence;

/**
 * Signals that stored data could not be written.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
