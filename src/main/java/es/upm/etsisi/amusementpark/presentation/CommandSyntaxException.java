package es.upm.etsisi.amusementpark.presentation;

/**
 * A command line is malformed (wrong arity or unparsable argument).
 */
public class CommandSyntaxException extends Exception {

    private static final long serialVersionUID = 1L;

    public CommandSyntaxException(String message) {
        super(message);
    }
}
