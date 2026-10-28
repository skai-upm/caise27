package es.upm.amusementpark.business.exception;

/**
 * Data does not satisfy the validation rules (DNI, email, password, card, price, capacity...).
 */
public class ValidationException extends ParkException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
