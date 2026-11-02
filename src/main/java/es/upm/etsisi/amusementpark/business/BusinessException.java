package es.upm.etsisi.amusementpark.business;

/**
 * Base class of the checked exceptions that signal a business operation could not be completed; the park state is left unchanged.
 */
public abstract class BusinessException extends Exception {

    private static final long serialVersionUID = 1L;

    protected BusinessException(String message) {
        super(message);
    }

    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
