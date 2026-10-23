package es.upm.amusementpark.business.exception;

/**
 * Input data does not have the required format or values.
 */
public class InvalidDataException extends ParkException {

    private static final long serialVersionUID = 1L;

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
