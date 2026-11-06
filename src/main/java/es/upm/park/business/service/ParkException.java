package es.upm.park.business.service;

/**
 * Signals that a park operation was rejected; the message explains why.
 */
public class ParkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParkException(String message) {
        super(message);
    }
}
