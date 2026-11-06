package es.upm.park.business.service;

/**
 * Signals that a group operation was rejected; the message explains why.
 */
public class GroupException extends ParkException {

    private static final long serialVersionUID = 1L;

    public GroupException(String message) {
        super(message);
    }
}
