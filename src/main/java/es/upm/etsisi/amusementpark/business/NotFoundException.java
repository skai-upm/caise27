package es.upm.etsisi.amusementpark.business;

/**
 * A referenced user, group, ticket, pass or attraction does not exist.
 */
public class NotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
