package es.upm.etsisi.park.user;

/** A user operation was rejected; the message is meant to be shown to the user. */
public class UserException extends Exception {

    public UserException(String message) {
        super(message);
    }
}
