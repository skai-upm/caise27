package es.upm.amusementpark.business.exception;

/**
 * Persistent data could not be read or written.
 */
public class DataAccessException extends ParkException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
