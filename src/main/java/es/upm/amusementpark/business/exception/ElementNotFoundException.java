package es.upm.amusementpark.business.exception;

/** A requested user, group, ticket or attraction does not exist. */
public class ElementNotFoundException extends ParkException {

    public ElementNotFoundException(String message) {
        super(message);
    }

    public ElementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
