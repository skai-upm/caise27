package es.upm.parque;

/**
 * A park operation was rejected. The message is meant to be shown to the user.
 */
public class ParkException extends RuntimeException {

    public ParkException(String message) {
        super(message);
    }
}
