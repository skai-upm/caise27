package es.upm.etsisi.amusementpark;

/**
 * A park operation was rejected; the message explains why.
 */
public class ParkException extends Exception {

    public ParkException(String message) {
        super(message);
    }

    public ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
