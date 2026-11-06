package es.upm.park.presentation;

/**
 * Outcome of processing one command line: the text to display and whether the
 * session must terminate after it.
 */
public record CommandResult(String output, boolean terminate) {

    public static CommandResult of(String output) {
        return new CommandResult(output, false);
    }

    public static CommandResult exit(String output) {
        return new CommandResult(output, true);
    }
}
