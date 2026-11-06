package es.upm.park.presentation.help;

import java.util.ArrayList;
import java.util.List;

/**
 * Help with the syntax of each command (the {@code HELP} output).
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        for (CommandInfo c : commands) {
            lines.add("  " + c.syntax());
            c.note().ifPresent(note -> lines.add("    " + note));
        }
        return String.join(System.lineSeparator(), lines);
    }
}
