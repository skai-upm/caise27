package es.upm.etsisi.park.business;

/**
 * Input that is malformed, invalid or duplicates an existing element.
 */
public class ValidationException extends ParkException {

    public ValidationException(String message) {
        super(message);
    }
}
