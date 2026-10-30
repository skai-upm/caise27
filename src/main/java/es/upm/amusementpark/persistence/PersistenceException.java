package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.exception.ParkException;

/**
 * Persistent data could not be read or written.
 */
public class PersistenceException extends ParkException {

    public PersistenceException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
