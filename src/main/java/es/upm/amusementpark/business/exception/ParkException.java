package es.upm.amusementpark.business.exception;

/**
 * Root of the exceptions that represent abnormal situations of park operations. Every subtype
 * carries a message describing the cause for the user.
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
