package es.upm.etsisi.amusementpark.business.exception;

/**
 * Base of the application exceptions that represent abnormal situations in park operations.
 */
public abstract class ParkException extends RuntimeException {

    public ParkException(String message) {
        super(message);
    }
}
