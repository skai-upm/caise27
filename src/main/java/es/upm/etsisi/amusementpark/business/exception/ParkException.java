package es.upm.etsisi.amusementpark.business.exception;

/**
 * Base of every business error: an operation that cannot be completed under the park rules.
 */
public abstract class ParkException extends RuntimeException {

    protected ParkException(String message) {
        super(message);
    }
}
