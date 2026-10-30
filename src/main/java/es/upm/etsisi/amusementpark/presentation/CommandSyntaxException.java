package es.upm.etsisi.amusementpark.presentation;

/**
 * A command line that cannot be interpreted (unknown command or wrong fields).
 */
public class CommandSyntaxException extends RuntimeException {

    public CommandSyntaxException(String message) {
        super(message);
    }
}
