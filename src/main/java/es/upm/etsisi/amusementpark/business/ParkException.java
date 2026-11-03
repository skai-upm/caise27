package es.upm.etsisi.amusementpark.business;

/**
 * Base of every abnormal situation raised by park operations. The command
 * layer reports these and keeps processing.
 */
public abstract class ParkException extends Exception {

    protected ParkException(String message) {
        super(message);
    }

    protected ParkException(String message, Throwable cause) {
        super(message, cause);
    }
}
