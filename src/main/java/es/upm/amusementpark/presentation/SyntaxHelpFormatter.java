package es.upm.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * HELP: one command syntax per line.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandHelp> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        for (CommandHelp command : commands) {
            lines.add(command.syntax());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
