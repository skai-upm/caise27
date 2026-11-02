package es.upm.etsisi.park.business;

/**
 * Signals that a referenced element does not exist.
 */
public class NotFoundException extends ParkException {

    public NotFoundException(String message) {
        super(message);
    }
}
