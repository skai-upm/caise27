package es.upm.amusementpark.business.exception;

/**
 * The operation is valid in form but forbidden by the current park state or rules.
 */
public class OperationNotAllowedException extends ParkException {

    private static final long serialVersionUID = 1L;

    public OperationNotAllowedException(String message) {
        super(message);
    }

    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
