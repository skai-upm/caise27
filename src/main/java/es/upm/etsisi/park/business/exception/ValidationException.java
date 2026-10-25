package es.upm.etsisi.park.business.exception;

/**
 * Input data does not satisfy the required format or rules.
 */
public class ValidationException extends ParkException {

    public ValidationException(String message) {
        super(message);
    }
}
