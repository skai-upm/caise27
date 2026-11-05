package es.upm.amusementpark.business.exception;

/**
 * The operation would break a business rule (uniqueness, locks, dates, credentials, capacity...).
 */
public class RuleViolationException extends OperationException {

    public RuleViolationException(String message) {
        super(message);
    }
}
