package es.upm.parque.presentation;

/**
 * Executes a command with its parameters and returns the text to show.
 */
@FunctionalInterface
public interface CommandHandler {

    String run(String[] params);
}
