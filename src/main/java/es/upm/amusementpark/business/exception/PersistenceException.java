package es.upm.amusementpark.business.exception;

/**
 * Park data could not be saved; the in-memory state was left unchanged.
 */
public class PersistenceException extends ParkException {

    private static final long serialVersionUID = 1L;

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
