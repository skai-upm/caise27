package es.upm.amusementpark.presentation;

/**
 * Signals that a command line cannot be interpreted: unknown command, wrong
 * number of elements or an element with an invalid value.
 */
public class InvalidCommandException extends Exception {

    public InvalidCommandException(String message) {
        super(message);
    }
}
