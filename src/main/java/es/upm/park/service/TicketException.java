package es.upm.park.service;

/**
 * Signals that a ticket operation was rejected; the message explains why.
 */
public class TicketException extends ParkException {

    private static final long serialVersionUID = 1L;

    public TicketException(String message) {
        super(message);
    }
}
