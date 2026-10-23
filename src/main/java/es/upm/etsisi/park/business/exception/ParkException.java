package es.upm.etsisi.park.business.exception;

/**
 * Base of the business exceptions: an operation could not be completed.
 * The message is meant to be shown to the user by the presentation layer.
 */
public class ParkException extends Exception {

    public ParkException(String message) {
        super(message);
    }

    public ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
