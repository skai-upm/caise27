package es.upm.etsisi.park.business.exception;

/** Stored data could not be written or read. */
public class PersistenceException extends ParkException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
