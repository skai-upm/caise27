package es.upm.park.service;

/**
 * Signals that an attraction operation was rejected; the message explains why.
 */
public class AttractionException extends ParkException {

    private static final long serialVersionUID = 1L;

    public AttractionException(String message) {
        super(message);
    }
}
