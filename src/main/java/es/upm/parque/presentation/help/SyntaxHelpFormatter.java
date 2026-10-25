package es.upm.parque.presentation.help;

import java.util.ArrayList;
import java.util.List;

import es.upm.parque.presentation.command.Command;

/**
 * Concise help: the syntax of every command.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<Command> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        for (Command command : commands) {
            for (String syntax : command.getSyntaxes()) {
                lines.add("  " + syntax);
            }
        }
        return String.join(System.lineSeparator(), lines);
    }
}
