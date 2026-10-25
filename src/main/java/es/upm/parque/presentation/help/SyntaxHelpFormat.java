package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Concise help: one syntax line per command.
 */
public class SyntaxHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandInfo c : commands) {
            text.append(System.lineSeparator()).append("  ").append(c.syntax());
        }
        return text.toString();
    }
}
