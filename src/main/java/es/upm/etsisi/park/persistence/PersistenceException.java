package es.upm.etsisi.park.persistence;

/**
 * Raised when the persistent storage cannot be read or written.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
