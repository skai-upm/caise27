package es.upm.amusementpark.presentation.help;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the syntax of each command followed by a brief explanation of its
 * purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public List<String> format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandInfo command : commands) {
            lines.add("  " + command.syntax());
            lines.add("      " + command.description());
        }
        return lines;
    }
}
