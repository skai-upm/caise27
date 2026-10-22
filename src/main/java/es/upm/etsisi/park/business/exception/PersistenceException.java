package es.upm.etsisi.park.business.exception;

/**
 * Data could not be read from or written to the persistent store.
 */
public class PersistenceException extends ParkException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
