package es.upm.amusementpark.park;

/**
 * Rejected park operation; the message is meant for the user.
 */
public class ParkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParkException(String message) {
        super(message);
    }
}
