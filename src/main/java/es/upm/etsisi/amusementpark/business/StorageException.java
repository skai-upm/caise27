package es.upm.etsisi.amusementpark.business;

/**
 * Thrown when persistent data could not be written.
 */
public class StorageException extends ParkException {
    public StorageException(String message) {
        super(message);
    }
}
