package es.upm.amusementpark.business.exception;

/**
 * Base of the checked exceptions raised when a park operation cannot be completed.
 */
public class ParkException extends Exception {

    public ParkException(String message) {
        super(message);
    }
}
