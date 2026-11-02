package es.upm.etsisi.amusementpark.presentation;

/**
 * Outcome of one command: the text to show and whether processing must stop.
 */
public record CommandResult(String message, boolean exit) {

    public static CommandResult message(String message) {
        return new CommandResult(message, false);
    }

    public static CommandResult error(String reason) {
        return new CommandResult("ERROR: " + reason, false);
    }
}
