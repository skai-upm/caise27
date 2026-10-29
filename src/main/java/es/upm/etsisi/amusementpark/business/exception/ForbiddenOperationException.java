package es.upm.etsisi.amusementpark.business.exception;

/**
 * Raised when the current state of the park does not allow the operation.
 */
public class ForbiddenOperationException extends ParkException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
