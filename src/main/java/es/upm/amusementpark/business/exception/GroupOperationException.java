package es.upm.amusementpark.business.exception;

/**
 * Signals that a group operation was rejected; the message explains why.
 */
public class GroupOperationException extends ParkException {

    public GroupOperationException(String message) {
        super(message);
    }
}
