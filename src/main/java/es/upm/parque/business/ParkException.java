package es.upm.parque.business;

/**
 * An operation was rejected; the message is shown to the person issuing the command.
 */
public class ParkException extends RuntimeException {

    public ParkException(String message) {
        super(message);
    }
}
