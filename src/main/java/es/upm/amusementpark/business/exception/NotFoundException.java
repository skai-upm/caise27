package es.upm.amusementpark.business.exception;

/**
 * A referenced element (user, group, ticket, pass, attraction) does not exist.
 */
public class NotFoundException extends ParkException {

    public NotFoundException(String message) {
        super(message);
    }
}
