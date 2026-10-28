package es.upm.etsisi.amusementpark.business.exception;

/**
 * A referenced user, group, ticket, attraction or membership does not exist.
 */
public class NotFoundException extends ParkException {

    public NotFoundException(String message) {
        super(message);
    }
}
