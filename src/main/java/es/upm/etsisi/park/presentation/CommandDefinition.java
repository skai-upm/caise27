package es.upm.etsisi.park.presentation;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Registry entry: keyword (the first {@code ;}-separated token), accepted syntax lines, purpose and handler.
 * The accepted parameter counts are derived from the syntax lines.
 */
public record CommandDefinition(String keyword, List<String> syntax, String description, Handler handler) {

    /** Executes a parsed command and returns the text to display. */
    @FunctionalInterface
    public interface Handler {
        String handle(String[] parts);
    }

    public CommandDefinition {
        syntax = List.copyOf(syntax);
    }

    /** Numbers of parameters allowed by the syntax lines (tokens after the keyword). */
    public Set<Integer> parameterCounts() {
        Set<Integer> counts = new TreeSet<>();
        for (String line : syntax) {
            counts.add(line.split(";", -1).length - 1);
        }
        return counts;
    }
}
