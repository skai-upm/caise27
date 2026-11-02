package es.upm.etsisi.park.business;

/**
 * Signals that persistent data could not be read or written.
 */
public class PersistenceException extends ParkException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
