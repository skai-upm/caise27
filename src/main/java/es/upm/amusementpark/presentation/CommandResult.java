package es.upm.amusementpark.presentation;

/**
 * Outcome of executing one command: the text to show and whether execution must stop.
 */
public record CommandResult(String output, boolean exit) {

    public static CommandResult message(String output) {
        return new CommandResult(output, false);
    }
}
