package es.upm.amusementpark.business.exception;

/**
 * Signals that an attraction operation was rejected; the message explains why.
 */
public class AttractionOperationException extends ParkException {

    public AttractionOperationException(String message) {
        super(message);
    }
}
