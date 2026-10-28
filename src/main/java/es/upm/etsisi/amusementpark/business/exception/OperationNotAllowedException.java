package es.upm.etsisi.amusementpark.business.exception;

/**
 * An operation violates a business rule in the current park state.
 */
public class OperationNotAllowedException extends ParkException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
