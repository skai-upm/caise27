package es.upm.parque.presentation.help;

import es.upm.parque.presentation.CommandDefinition;

import java.util.List;

/**
 * Extended help: syntax plus a brief description of each command.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDefinition> commands) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("Available commands (extended):");
        for (CommandDefinition c : commands) {
            sb.append(nl).append("  ").append(c.syntax()).append(nl).append("      ").append(c.description());
        }
        return sb.toString();
    }
}
