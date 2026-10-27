package es.upm.amusementpark.business.exception;

/**
 * Signals that an invoice could not be generated; the message explains why.
 */
public class InvoiceOperationException extends ParkException {

    public InvoiceOperationException(String message) {
        super(message);
    }
}
