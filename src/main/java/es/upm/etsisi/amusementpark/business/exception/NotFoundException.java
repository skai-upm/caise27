package es.upm.etsisi.amusementpark.business.exception;

/**
 * Raised when a referenced user, group, ticket, pass or attraction does not exist.
 */
public class NotFoundException extends ParkException {

    public NotFoundException(String message) {
        super(message);
    }
}
