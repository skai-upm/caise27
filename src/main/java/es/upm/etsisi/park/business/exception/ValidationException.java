package es.upm.etsisi.park.business.exception;

/** An input value does not satisfy the validation rules (format, range, emptiness). */
public class ValidationException extends ParkException {

    public ValidationException(String message) {
        super(message);
    }
}
