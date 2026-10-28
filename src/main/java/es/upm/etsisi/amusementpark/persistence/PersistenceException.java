package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.exception.ParkException;

/**
 * Persistent data could not be read or written.
 */
public class PersistenceException extends ParkException {

    public PersistenceException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
