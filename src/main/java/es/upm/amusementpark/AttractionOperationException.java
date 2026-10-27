package es.upm.amusementpark;

/**
 * Signals that an attraction operation was rejected; the message explains why.
 */
public class AttractionOperationException extends Exception {

    public AttractionOperationException(String message) {
        super(message);
    }
}
