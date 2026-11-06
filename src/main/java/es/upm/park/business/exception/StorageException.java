package es.upm.park.business.exception;

/** Persistent park data could not be read or written. */
public class StorageException extends ParkException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
