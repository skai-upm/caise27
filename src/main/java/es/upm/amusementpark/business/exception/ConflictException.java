package es.upm.amusementpark.business.exception;

/**
 * The operation would duplicate an existing element or break a uniqueness rule.
 */
public class ConflictException extends ParkException {

    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }
}
