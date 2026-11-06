package es.upm.park.presentation.help;

import java.util.ArrayList;
import java.util.List;

/**
 * Help with the syntax and a brief purpose of each command (the
 * {@code HELP EXTENDED} output).
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandInfo c : commands) {
            lines.add("  " + c.syntax());
            lines.add("      - " + c.purpose());
            c.note().ifPresent(note -> lines.add("      " + note));
        }
        return String.join(System.lineSeparator(), lines);
    }
}
