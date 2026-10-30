package es.upm.etsisi.amusementpark.presentation.command;

/**
 * Executes a parsed command and returns the text to show.
 */
@FunctionalInterface
public interface CommandAction {

    String execute(String[] fields);
}
