package es.upm.amusementpark.presentation;

import es.upm.amusementpark.presentation.command.CommandDefinition;
import es.upm.amusementpark.presentation.command.CommandRegistry;

import java.io.PrintStream;

/**
 * Interprets and executes text commands through the command registry. Shared by every input mode.
 */
public class CommandProcessor {

    private final CommandRegistry registry;
    private final PrintStream out;

    public CommandProcessor(CommandRegistry registry, PrintStream out) {
        this.registry = registry;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return {@code false} when processing must stop, {@code true} otherwise
     */
    public boolean process(String line) {
        try {
            return execute(line.trim());
        } catch (RuntimeException e) {
            out.println("ERROR: could not process command: " + line);
            return true;
        }
    }

    private boolean execute(String line) {
        String[] parts = line.split(";", -1);
        CommandDefinition command = registry.find(parts[0].trim());
        if (command == null) {
            out.println("ERROR: unknown command: " + line);
            return true;
        }
        if (!command.accepts(parts.length)) {
            out.println("ERROR: wrong number of parameters. Usage: " + String.join(" | ", command.syntaxes()));
            return true;
        }
        if (command.terminates()) {
            out.println("Bye");
            return false;
        }
        out.println(command.handler().apply(parts));
        return true;
    }
}
