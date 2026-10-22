package es.upm.etsisi.park.business.exception;

/**
 * An identifier or unique value (DNI, email) is already in use.
 */
public class DuplicateException extends ParkException {

    public DuplicateException(String message) {
        super(message);
    }
}
