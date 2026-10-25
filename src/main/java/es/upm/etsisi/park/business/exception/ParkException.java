package es.upm.etsisi.park.business.exception;

/**
 * Base class for every business rule violation. The message is meant to be shown to the user.
 */
public class ParkException extends RuntimeException {

    public ParkException(String message) {
        super(message);
    }
}
