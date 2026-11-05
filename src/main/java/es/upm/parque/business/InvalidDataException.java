package es.upm.parque.business;

/**
 * A value supplied to an operation does not satisfy the validation rules.
 */
public class InvalidDataException extends ParkException {

    private static final long serialVersionUID = 1L;

    public InvalidDataException(String message) {
        super(message);
    }
}
