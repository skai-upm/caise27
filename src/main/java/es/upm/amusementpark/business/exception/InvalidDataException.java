package es.upm.amusementpark.business.exception;

/**
 * Supplied data does not satisfy the format or validation rules.
 */
public class InvalidDataException extends OperationException {

    public InvalidDataException(String message) {
        super(message);
    }
}
