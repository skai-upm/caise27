package es.upm.amusementpark.presentation;

/**
 * A command line that cannot be interpreted: unknown command or wrong syntax.
 */
public class InvalidCommandException extends Exception {

    public InvalidCommandException(String message) {
        super(message);
    }
}
