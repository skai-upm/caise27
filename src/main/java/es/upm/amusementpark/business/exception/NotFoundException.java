package es.upm.amusementpark.business.exception;

/**
 * A referenced element (user, group, ticket, attraction...) does not exist.
 */
public class NotFoundException extends OperationException {

    public NotFoundException(String message) {
        super(message);
    }
}
