package es.upm.amusementpark.presentation.help;

import java.util.ArrayList;
import java.util.List;

/** Concise help: one line with the syntax of each command. */
public class SyntaxHelpFormatter implements HelpFormatter {

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
