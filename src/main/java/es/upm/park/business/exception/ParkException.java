package es.upm.park.business.exception;

/**
 * Base of the exceptions that represent abnormal situations in park operations.
 * Their messages are meant to be shown to the user by the presentation layer.
 */
public abstract class ParkException extends RuntimeException {

    protected ParkException(String message) {
        super(message);
    }

    protected ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
