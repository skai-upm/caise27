package es.upm.etsisi.amusementpark.business;

/**
 * A business rule forbids the requested operation.
 */
public class RuleViolationException extends ParkException {

    public RuleViolationException(String message) {
        super(message);
    }
}
