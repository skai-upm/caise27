package es.upm.amusementpark.business.exception;

/**
 * Root of the exceptions that represent abnormal situations in park operations. The message explains
 * the reason and is shown to the user by the presentation layer.
 */
public class ParkException extends Exception {

    public ParkException(String message) {
        super(message);
    }

    public ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
