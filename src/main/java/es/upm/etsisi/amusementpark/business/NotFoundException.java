package es.upm.etsisi.amusementpark.business;

/**
 * Thrown when a referenced user, group, ticket, attraction or pass does not exist.
 */
public class NotFoundException extends ParkException {
    public NotFoundException(String message) {
        super(message);
    }
}
