package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * HELP: the syntax of each command.
 */
public class SyntaxHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (CommandInfo c : commands) {
            sb.append(System.lineSeparator()).append("  ").append(c.syntax());
            if (!c.note().isEmpty()) {
                sb.append("   ").append(c.note());
            }
        }
        return sb.toString();
    }
}
