package es.upm.etsisi.park.presentation.help;

import java.util.ArrayList;
import java.util.List;

/**
 * HELP: one line per command syntax.
 */
public class SimpleHelpFormatter implements HelpFormatter {

    @Override
    public List<String> format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        for (CommandInfo command : commands) {
            for (String syntax : command.syntaxes()) {
                lines.add("  " + syntax);
            }
        }
        return lines;
    }
}
