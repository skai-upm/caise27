package es.upm.amusementpark.business.exception;

/**
 * Root of the exceptions used to report park operations that cannot be completed.
 */
public class ParkException extends RuntimeException {

    public ParkException(String message) {
        super(message);
    }
}
