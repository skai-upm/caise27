package es.upm.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * HELP EXTENDED: each syntax followed by an indented description of its purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandHelp> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandHelp command : commands) {
            lines.add(command.syntax());
            lines.add("    " + command.description());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
