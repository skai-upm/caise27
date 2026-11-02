package es.upm.etsisi.park.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * HELP format: every accepted syntax line.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDefinition> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        commands.forEach(c -> lines.addAll(c.syntax()));
        return String.join(System.lineSeparator(), lines);
    }
}
