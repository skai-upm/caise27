package es.upm.amusementpark.business.exception;

/**
 * A referenced user, group, ticket, pass or attraction does not exist.
 */
public class NotFoundException extends ParkException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
