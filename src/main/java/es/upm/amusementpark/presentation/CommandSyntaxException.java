package es.upm.amusementpark.presentation;

/**
 * A command line cannot be interpreted (unknown command, wrong number of fields, bad option).
 */
public class CommandSyntaxException extends Exception {

    public CommandSyntaxException(String message) {
        super(message);
    }
}
