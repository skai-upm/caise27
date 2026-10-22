package es.upm.etsisi.park.business.exception;

/**
 * Input data with a wrong format or value (dates, prices, DNI, email, quantities...).
 */
public class ValidationException extends ParkException {

    public ValidationException(String message) {
        super(message);
    }
}
