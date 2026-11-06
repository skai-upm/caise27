package es.upm.park.service;

/**
 * Signals that a user operation was rejected; the message explains why.
 */
public class UserException extends ParkException {

    private static final long serialVersionUID = 1L;

    public UserException(String message) {
        super(message);
    }
}
