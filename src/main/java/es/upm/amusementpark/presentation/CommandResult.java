package es.upm.amusementpark.presentation;

/**
 * Outcome of processing one command line: the text to show and whether the
 * application must stop.
 *
 * @param output text to print, possibly empty
 * @param exit   {@code true} if no further command must be processed
 */
public record CommandResult(String output, boolean exit) {

    public static CommandResult of(String output) {
        return new CommandResult(output, false);
    }

    public static CommandResult error(String message) {
        return new CommandResult("ERROR: " + message, false);
    }
}
