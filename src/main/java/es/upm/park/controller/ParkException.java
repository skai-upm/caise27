package es.upm.park.controller;

/**
 * A park operation was rejected because a business rule or argument was not
 * satisfied. The message is meant to be shown to the user.
 */
public class ParkException extends RuntimeException {

    public ParkException(String message) {
        super(message);
    }
}
