package es.upm.etsisi.park.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * HELP EXTENDED format: syntax lines followed by a brief explanation of the command's purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDefinition> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandDefinition c : commands) {
            lines.addAll(c.syntax());
            lines.add("    " + c.description());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
