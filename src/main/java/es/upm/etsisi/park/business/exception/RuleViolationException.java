package es.upm.etsisi.park.business.exception;

/**
 * The request is well formed but a park rule forbids it (locks, dates, capacity, passes,
 * accepted ticket types).
 */
public class RuleViolationException extends ParkException {

    public RuleViolationException(String message) {
        super(message);
    }
}
