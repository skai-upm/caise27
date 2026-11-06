package es.upm.park.business.exception;

/** A requested element (user, group, ticket or attraction) does not exist. */
public class NotFoundException extends ParkException {

    public NotFoundException(String message) {
        super(message);
    }
}
