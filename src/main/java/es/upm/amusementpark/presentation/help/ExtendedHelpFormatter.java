package es.upm.amusementpark.presentation.help;

import java.util.ArrayList;
import java.util.List;

/** Extended help: the syntax of each command followed by its description. */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandInfo command : commands) {
            lines.add("  " + command.syntax());
            lines.add("      " + command.description());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
