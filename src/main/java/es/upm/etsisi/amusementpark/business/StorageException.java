package es.upm.etsisi.amusementpark.business;

/**
 * Persistent data could not be read or written.
 */
public class StorageException extends ParkException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
