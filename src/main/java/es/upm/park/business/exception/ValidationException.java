package es.upm.park.business.exception;

/**
 * A value does not satisfy the format or rules required by an operation.
 */
public class ValidationException extends ParkException {

    public ValidationException(String message) {
        super(message);
    }
}
