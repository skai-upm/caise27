package es.upm.park.business.service;

/**
 * Signals that a pass operation was rejected; the message explains why.
 */
public class PassException extends ParkException {

    private static final long serialVersionUID = 1L;

    public PassException(String message) {
        super(message);
    }
}
