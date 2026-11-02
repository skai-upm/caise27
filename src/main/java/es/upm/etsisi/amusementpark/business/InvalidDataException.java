package es.upm.etsisi.amusementpark.business;

/**
 * Supplied data does not satisfy the validation rules.
 */
public class InvalidDataException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public InvalidDataException(String message) {
        super(message);
    }
}
