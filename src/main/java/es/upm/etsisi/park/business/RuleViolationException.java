package es.upm.etsisi.park.business;

/**
 * Signals that a business rule forbids the operation.
 */
public class RuleViolationException extends ParkException {

    public RuleViolationException(String message) {
        super(message);
    }
}
