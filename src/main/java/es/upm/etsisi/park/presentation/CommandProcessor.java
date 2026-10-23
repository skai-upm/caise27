package es.upm.etsisi.park.presentation;

import java.io.PrintStream;

import es.upm.etsisi.park.business.exception.ParkException;

/**
 * Interprets one text command per line. Elements are separated by ';'; the
 * first element is the command keyword and the rest are its arguments.
 * The same processor is used for interactive and file input. It is the
 * place where business exceptions become {@code ERROR:} lines, so a failing
 * command never stops processing.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private final CommandRegistry registry;
    private final PrintStream out;

    public CommandProcessor(CommandRegistry registry, PrintStream out) {
        this.registry = registry;
        this.out = out;
    }

    /**
     * Processes a single command line.
     *
     * @return false if the application must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return true;
        }
        String[] parts = line.split(SEPARATOR, -1);
        String keyword = normalizeKeyword(parts[0]);
        String[] args = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            args[i - 1] = parts[i].trim();
        }
        CommandDefinition command = registry.find(keyword);
        if (command == null) {
            error("Unknown command '" + parts[0].trim() + "'. Type HELP to list the commands");
            return true;
        }
        try {
            command.handler().execute(args, out);
            return !command.terminates();
        } catch (ParkException e) {
            error(e.getMessage());
        } catch (RuntimeException e) {
            error("Could not process command: " + e.getMessage());
        }
        return true;
    }

    private void error(String message) {
        out.println("ERROR: " + message);
    }

    /** Trims, collapses inner spaces and upper-cases the keyword so "user  create" matches "USER CREATE". */
    static String normalizeKeyword(String raw) {
        StringBuilder sb = new StringBuilder();
        for (String token : raw.trim().split(" ")) {
            if (!token.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(token);
            }
        }
        return sb.toString().toUpperCase();
    }
}
