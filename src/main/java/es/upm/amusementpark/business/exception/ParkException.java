package es.upm.amusementpark.business.exception;

/** Root of the abnormal situations raised by park business operations. */
public class ParkException extends RuntimeException {

    public ParkException(String message) {
        super(message);
    }

    public ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
