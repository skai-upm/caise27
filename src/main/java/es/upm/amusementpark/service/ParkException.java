package es.upm.amusementpark.service;

/**
 * Signals that a park operation was rejected; the message explains why.
 */
public class ParkException extends Exception {

    private static final long serialVersionUID = 1L;

    public ParkException(String message) {
        super(message);
    }

    public ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
