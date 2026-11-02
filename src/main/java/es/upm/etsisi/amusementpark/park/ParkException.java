package es.upm.etsisi.amusementpark.park;

import es.upm.etsisi.amusementpark.user.UserException;

/**
 * Signals that a park operation was rejected; the park state is left unchanged.
 */
public class ParkException extends UserException {

    private static final long serialVersionUID = 1L;

    public ParkException(String message) {
        super(message);
    }
}
