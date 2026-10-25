package es.upm.parque.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Interprets one text command per call using the registered commands. Shared by interactive
 * and file modes.
 */
public class CommandProcessor {

    public static final String EXIT = "EXIT";

    private final CommandRegistry registry;
    private final PrintStream out;

    public CommandProcessor(CommandRegistry registry, PrintStream out) {
        this.registry = registry;
        this.out = out;
    }

    /**
     * Processes a command line and prints its result.
     *
     * @return false when the command was EXIT, true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.split(";", -1);
        String name = parts[0].trim().toUpperCase();
        String[] params = Arrays.copyOfRange(parts, 1, parts.length);
        for (int i = 0; i < params.length; i++) {
            params[i] = params[i].trim();
        }
        CommandRegistry.Match match = registry.find(name, params);
        if (match == null) {
            out.println("ERROR Unknown command: " + parts[0].trim() + " (type HELP)");
            return true;
        }
        Command command = match.command();
        if (match.keywordInName()) {
            params = Arrays.copyOfRange(params, 1, params.length);
        }
        if (params.length < command.minParams() || params.length > command.maxParams()) {
            out.println("ERROR " + command.name() + " expects " + expected(command) + " parameter(s) but received "
                    + params.length + ". Usage: " + command.syntax());
            return true;
        }
        try {
            out.println(command.handler().run(params));
        } catch (IllegalArgumentException e) {
            out.println("ERROR " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR Unexpected failure: " + e.getMessage());
        }
        return !EXIT.equals(name);
    }

    /**
     * Processes every non-blank line until EXIT or the end of the input.
     */
    public void run(BufferedReader reader) throws IOException {
        String line;
        boolean running = true;
        while (running && (line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                running = process(line);
            }
        }
    }

    private static String expected(Command command) {
        return command.minParams() == command.maxParams()
                ? String.valueOf(command.minParams())
                : command.minParams() + "-" + command.maxParams();
    }
}
