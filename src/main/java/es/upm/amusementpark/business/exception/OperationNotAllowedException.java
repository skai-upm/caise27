package es.upm.amusementpark.business.exception;

/** A business rule forbids the requested operation in the current state. */
public class OperationNotAllowedException extends ParkException {

    public OperationNotAllowedException(String message) {
        super(message);
    }

    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
