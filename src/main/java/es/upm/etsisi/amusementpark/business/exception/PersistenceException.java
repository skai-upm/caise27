package es.upm.etsisi.amusementpark.business.exception;

/**
 * Raised by repositories when persistent data cannot be read or written.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
