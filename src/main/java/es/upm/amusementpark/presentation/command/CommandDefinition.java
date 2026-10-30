package es.upm.amusementpark.presentation.command;

import java.util.List;
import java.util.function.Function;

/**
 * A registered text command: its name (the first {@code ;}-separated token), the accepted syntax forms,
 * a description for extended help, the accepted number of {@code ;}-separated parts and its handler.
 * A {@code null} handler marks the command that terminates processing (EXIT).
 */
public record CommandDefinition(String name, List<String> syntaxes, String description, int minParts, int maxParts,
        Function<String[], String> handler) {

    public CommandDefinition {
        syntaxes = List.copyOf(syntaxes);
    }

    public static CommandDefinition of(String syntax, String description, Function<String[], String> handler) {
        int parts = syntax.split(";", -1).length;
        return new CommandDefinition(syntax.split(";", -1)[0], List.of(syntax), description, parts, parts, handler);
    }

    public boolean accepts(int parts) {
        return parts >= minParts && parts <= maxParts;
    }

    public boolean terminates() {
        return handler == null;
    }
}
