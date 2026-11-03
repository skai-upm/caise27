package es.upm.etsisi.park.business;

/**
 * The park state could not be saved; the in-memory change has been undone.
 */
public class PersistenceException extends ParkException {

    public PersistenceException(String message) {
        super(message);
    }
}
