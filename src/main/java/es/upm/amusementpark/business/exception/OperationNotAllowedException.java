package es.upm.amusementpark.business.exception;

/**
 * The park rules forbid the operation in the current state (locks, dates, capacity, credentials).
 */
public class OperationNotAllowedException extends ParkException {

    private static final long serialVersionUID = 1L;

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
