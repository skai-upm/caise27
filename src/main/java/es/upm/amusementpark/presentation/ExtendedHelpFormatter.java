package es.upm.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Help listing each command's syntax followed by its description.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandInfo c : commands) {
            lines.add("  " + c.syntax());
            lines.add("      " + c.description());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
