package es.upm.etsisi.park.business.exception;

/**
 * Base of every abnormal situation reported by the park. Unchecked so it can cross the
 * layers; the presentation layer catches it and reports its message.
 */
public class ParkException extends RuntimeException {

    public ParkException(String message) {
        super(message);
    }

    public ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
