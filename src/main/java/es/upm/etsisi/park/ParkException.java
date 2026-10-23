package es.upm.etsisi.park;

/** A park operation was rejected; the message is meant to be shown to the user. */
public class ParkException extends Exception {

    public ParkException(String message) {
        super(message);
    }
}
