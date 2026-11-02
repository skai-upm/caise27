package es.upm.etsisi.park;

/**
 * Signals that a park operation was rejected; the message is shown to the operator.
 */
public class ParkException extends RuntimeException {

    public ParkException(String message) {
        super(message);
    }

    public ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
