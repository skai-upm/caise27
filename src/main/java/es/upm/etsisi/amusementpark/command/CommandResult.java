package es.upm.etsisi.amusementpark.command;

/**
 * Outcome of processing one command line.
 *
 * @param output text to show to the user
 * @param exit   whether processing must stop after this command
 */
public record CommandResult(String output, boolean exit) {

    static CommandResult message(String output) {
        return new CommandResult(output, false);
    }

    static CommandResult error(String reason) {
        return new CommandResult("ERROR: " + reason, false);
    }
}
