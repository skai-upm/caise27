package es.upm.etsisi.amusementpark.business;

/**
 * Park data could not be read or written.
 */
public class StorageException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
