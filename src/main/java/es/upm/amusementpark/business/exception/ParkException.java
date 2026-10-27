package es.upm.amusementpark.business.exception;

/**
 * Base of the exceptions that represent a park operation rejected by the
 * business rules. The message explains why.
 */
public class ParkException extends Exception {

    public ParkException(String message) {
        super(message);
    }
}
