package es.upm.amusementpark.business.exception;

/**
 * A referenced user, group, ticket or attraction does not exist.
 */
public class NotFoundException extends ParkException {

    public NotFoundException(String message) {
        super(message);
    }
}
