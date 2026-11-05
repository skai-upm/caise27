package es.upm.amusementpark.presentation;

/**
 * A command line that cannot be interpreted (wrong number of fields or badly formatted values). Raised by command
 * handlers and handled centrally by {@link CommandProcessor}.
 */
public class InvalidCommandException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidCommandException(String message) {
        super(message);
    }
}
