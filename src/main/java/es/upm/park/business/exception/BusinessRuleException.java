package es.upm.park.business.exception;

/**
 * The park state does not allow the operation (dates, locks, consumed passes...).
 */
public class BusinessRuleException extends ParkException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
