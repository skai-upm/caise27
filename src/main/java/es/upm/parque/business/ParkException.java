package es.upm.parque.business;

/**
 * Base of the abnormal situations raised by business operations; its message describes the problem for the user.
 */
public class ParkException extends Exception {

    private static final long serialVersionUID = 1L;

    public ParkException(String message) {
        super(message);
    }
}
