package es.upm.parque.business;

/**
 * An element referenced by an operation (user, group, ticket, attraction) does not exist.
 */
public class NotFoundException extends ParkException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
