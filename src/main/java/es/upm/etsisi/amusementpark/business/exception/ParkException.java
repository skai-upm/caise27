package es.upm.etsisi.amusementpark.business.exception;

/**
 * Base of the checked exceptions that business operations raise when they cannot be completed.
 */
public abstract class ParkException extends Exception {

    protected ParkException(String message) {
        super(message);
    }
}
