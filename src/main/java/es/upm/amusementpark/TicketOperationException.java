package es.upm.amusementpark;

/**
 * Signals that a ticket operation was rejected; the message explains why.
 */
public class TicketOperationException extends Exception {

    public TicketOperationException(String message) {
        super(message);
    }
}
