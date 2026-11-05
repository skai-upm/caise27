package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * HELP EXTENDED: the syntax of each command followed by a brief explanation of its purpose.
 */
public class ExtendedHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder sb = new StringBuilder("Available commands (extended):");
        String nl = System.lineSeparator();
        for (CommandInfo c : commands) {
            sb.append(nl).append("  ").append(c.syntax());
            if (!c.note().isEmpty()) {
                sb.append("   ").append(c.note());
            }
            sb.append(nl).append("      - ").append(c.purpose());
        }
        return sb.toString();
    }
}
