package es.upm.amusementpark.business.exception;

/**
 * Root of the exceptions that represent abnormal situations in park operations. They propagate up to
 * the presentation layer, which reports them without stopping command processing.
 */
public abstract class ParkException extends RuntimeException {

    protected ParkException(String message) {
        super(message);
    }

    protected ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
