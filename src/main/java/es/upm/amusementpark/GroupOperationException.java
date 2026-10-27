package es.upm.amusementpark;

/**
 * Signals that a group operation was rejected; the message explains why.
 */
public class GroupOperationException extends Exception {

    public GroupOperationException(String message) {
        super(message);
    }
}
