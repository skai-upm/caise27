package es.upm.park.presentation;

/**
 * Outcome of processing one command line: the text to show and whether the
 * application must stop processing further commands.
 */
public record CommandResult(String output, boolean exit) {

    public static CommandResult message(String output) {
        return new CommandResult(output, false);
    }

    public static CommandResult error(String message) {
        return new CommandResult("ERROR: " + message, false);
    }

    public static CommandResult exit(String output) {
        return new CommandResult(output, true);
    }
}
