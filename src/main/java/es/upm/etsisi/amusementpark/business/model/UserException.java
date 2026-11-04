package es.upm.etsisi.amusementpark.business.model;

/** Signals that a user operation was rejected; the message explains why. */
public class UserException extends Exception {

    private static final long serialVersionUID = 1L;

    public UserException(String message) {
        super(message);
    }
}
