package es.upm.parque.presentation;

/**
 * Executes a command with its parameters (the components after the operation) and returns the response.
 */
@FunctionalInterface
public interface CommandAction {

    String execute(String[] parameters) throws Exception;
}
