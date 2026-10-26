package es.upm.parque.business;

/**
 * Signals that a user operation was rejected; the message explains why.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
