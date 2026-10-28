package es.upm.etsisi.amusementpark.business.exception;

/**
 * An identifier, e-mail or membership is already in use.
 */
public class AlreadyExistsException extends ParkException {

    public AlreadyExistsException(String message) {
        super(message);
    }
}
