package es.upm.parque.presentation;

/**
 * A command line does not have the expected syntax (command name or number of elements).
 */
public class InvalidCommandException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidCommandException(String message) {
        super(message);
    }
}
