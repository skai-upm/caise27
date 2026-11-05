package es.upm.amusementpark.presentation;

/**
 * Handles one command keyword. Implementations receive the full trimmed command line.
 */
@FunctionalInterface
public interface CommandHandler {

    /**
     * Executes the command.
     *
     * @param line the full trimmed command line
     * @return {@code true} to keep processing further commands, {@code false} to stop the session
     */
    boolean handle(String line);
}
