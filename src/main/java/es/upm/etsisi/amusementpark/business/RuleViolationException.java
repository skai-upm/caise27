package es.upm.etsisi.amusementpark.business;

/**
 * Thrown when the operation is not allowed by a business rule (duplicates, locks, dates, capacity, access type).
 */
public class RuleViolationException extends ParkException {
    public RuleViolationException(String message) {
        super(message);
    }
}
