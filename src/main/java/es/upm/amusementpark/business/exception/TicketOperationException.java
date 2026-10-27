package es.upm.amusementpark.business.exception;

/**
 * Signals that a ticket operation was rejected; the message explains why.
 */
public class TicketOperationException extends ParkException {

    public TicketOperationException(String message) {
        super(message);
    }
}
