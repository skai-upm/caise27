package es.upm.etsisi.park.presentation.help;

import java.util.ArrayList;
import java.util.List;

/**
 * HELP EXTENDED: every syntax followed by a short description of the command's purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public List<String> format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandInfo command : commands) {
            for (String syntax : command.syntaxes()) {
                lines.add("  " + syntax);
            }
            lines.add("      " + command.description());
        }
        return lines;
    }
}
