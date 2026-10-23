package es.upm.etsisi.park.user;

import es.upm.etsisi.park.ParkException;

/** A user operation was rejected; the message is meant to be shown to the user. */
public class UserException extends ParkException {

    public UserException(String message) {
        super(message);
    }
}
