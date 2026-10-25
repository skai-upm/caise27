package es.upm.parque.presentation;

import java.util.Arrays;

import es.upm.parque.presentation.command.Command;
import es.upm.parque.presentation.command.CommandRegistry;

/**
 * Interprets text commands of the form {@code OPERATION;param1;param2;...} and executes the registered command.
 * The same processor is used for interactive input and for command files.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private final CommandRegistry registry;
    private boolean finished;

    public CommandProcessor(CommandRegistry registry) {
        this.registry = registry;
    }

    /**
     * Returns true once EXIT has been processed; no further commands should be supplied.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Stops processing after the current command.
     */
    public void finish() {
        finished = true;
    }

    /**
     * Executes one command line and returns the message to show, or {@code null} for a blank line.
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.trim().split(SEPARATOR, -1);
        String keyword = parts[0].trim();
        String[] parameters = Arrays.copyOfRange(parts, 1, parts.length);
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = parameters[i].trim();
        }
        Command command = registry.find(keyword);
        if (command == null) {
            return "ERROR Unknown command: " + keyword + ". Type HELP to see the available commands";
        }
        if (!command.acceptsParameterCount(parameters.length)) {
            return "ERROR Wrong number of parameters. Usage: " + String.join(" | ", command.getSyntaxes());
        }
        return command.execute(parameters);
    }
}
