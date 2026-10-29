package es.upm.etsisi.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Help listing the syntax of each command followed by a brief explanation of its purpose.
 */
public class ExtendedHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandInfo command : commands) {
            lines.add(command.syntax());
            lines.add("    " + command.description());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
