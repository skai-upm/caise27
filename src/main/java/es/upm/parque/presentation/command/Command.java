package es.upm.parque.presentation.command;

import java.util.List;

/**
 * A text command the application understands, e.g. {@code USER CREATE;...}.
 */
public interface Command {

    /**
     * The operation part of the command line, e.g. {@code USER CREATE}.
     */
    String getKeyword();

    /**
     * Accepted syntaxes, with the parameters separated by ';'.
     */
    List<String> getSyntaxes();

    String getDescription();

    boolean acceptsParameterCount(int count);

    /**
     * Executes the command and returns the message to show.
     */
    String execute(String[] parameters);
}
