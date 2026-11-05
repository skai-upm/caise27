package es.upm.parque.business;

/**
 * An operation would break a business rule (uniqueness, locks, capacity, historical records...).
 */
public class RuleViolationException extends ParkException {

    private static final long serialVersionUID = 1L;

    public RuleViolationException(String message) {
        super(message);
    }
}
