package es.upm.etsisi.park.business.exception;

/** A referenced user, group, ticket, pass or attraction does not exist. */
public class NotFoundException extends ParkException {

    public NotFoundException(String message) {
        super(message);
    }
}
