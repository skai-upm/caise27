package es.upm.parque.presentation.help;

import es.upm.parque.presentation.CommandDefinition;

import java.util.List;

/**
 * Concise help: one syntax line per command.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDefinition> commands) {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (CommandDefinition c : commands) {
            sb.append(System.lineSeparator()).append("  ").append(c.syntax());
        }
        return sb.toString();
    }
}
