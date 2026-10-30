package es.upm.amusementpark.business.model;

/**
 * Raised when a ticket cannot be used to enter an attraction. No state has been changed when it is thrown.
 */
public class AccessDeniedException extends Exception {

    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message);
    }
}
