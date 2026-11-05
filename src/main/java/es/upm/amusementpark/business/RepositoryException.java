package es.upm.amusementpark.business;

/**
 * The park state could not be read from or written to its repository.
 */
public class RepositoryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
