package es.upm.etsisi.park.presentation;

/**
 * Outcome of processing one command line.
 *
 * @param output text to show to the user (may span several lines, may be empty)
 * @param exit   whether the application must stop after this command
 */
public record CommandResult(String output, boolean exit) {

    public static CommandResult of(String output) {
        return new CommandResult(output, false);
    }

    public static CommandResult error(String message) {
        return new CommandResult("ERROR: " + message, false);
    }

    public static CommandResult exit(String output) {
        return new CommandResult(output, true);
    }
}
