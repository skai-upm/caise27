package es.upm.amusementpark.presentation.help;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists each command's syntax, one per line.
 */
public class ConciseHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        for (CommandInfo command : commands) {
            lines.add("  " + command.syntax());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
