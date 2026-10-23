package es.upm.amusementpark.business.exception;

/**
 * Base of every rejected or failed park operation; the message explains why.
 */
public abstract class ParkException extends Exception {

    private static final long serialVersionUID = 1L;

    protected ParkException(String message) {
        super(message);
    }

    protected ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
