package es.upm.parque.presentation.help;

import java.util.ArrayList;
import java.util.List;

import es.upm.parque.presentation.command.Command;

/**
 * Extended help: the syntax of every command followed by a description of its behavior.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<Command> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        for (Command command : commands) {
            for (String syntax : command.getSyntaxes()) {
                lines.add("  " + syntax);
            }
            lines.add("      " + command.getDescription());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
