package es.upm.parque.presentation;

import java.util.List;

/**
 * Concise help: one line with the syntax of each command.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax());
        }
        return text.toString();
    }
}
