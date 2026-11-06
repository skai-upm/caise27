package es.upm.park.business.exception;

/** An operation is not allowed by the park's business rules in the current state. */
public class BusinessRuleException extends ParkException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
