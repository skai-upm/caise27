package es.upm.etsisi.park;

/**
 * Signals that a user operation was rejected; the message is shown to the operator.
 */
public class UserException extends ParkException {

    public UserException(String message) {
        super(message);
    }
}
