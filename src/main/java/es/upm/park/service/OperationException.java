package es.upm.park.service;

/**
 * Raised when a park operation is rejected. The message is shown to the
 * person issuing the command.
 */
public class OperationException extends Exception {

    public OperationException(String message) {
        super(message);
    }
}
