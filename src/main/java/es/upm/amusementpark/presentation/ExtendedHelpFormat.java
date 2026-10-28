package es.upm.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Syntax of each command followed by a brief explanation of its purpose.
 */
public class ExtendedHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandDescriptor> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandDescriptor c : commands) {
            lines.add(c.syntax());
            lines.add("    " + c.description());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
