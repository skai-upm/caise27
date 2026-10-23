package es.upm.park.business.exception;

/**
 * Base of the checked exceptions raised when a park operation cannot be
 * completed. The message is meant for the person issuing the command.
 */
public class ParkException extends Exception {

    public ParkException(String message) {
        super(message);
    }
}
