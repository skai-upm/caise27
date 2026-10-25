package es.upm.parque.presentation.help;

import es.upm.parque.presentation.CommandDefinition;

import java.util.List;

/**
 * Syntax of each command followed by a short description of its behaviour.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDefinition> commands) {
        StringBuilder text = new StringBuilder("Available commands (extended):");
        commands.forEach(command -> text.append(System.lineSeparator()).append("  ").append(command.syntax())
                .append(System.lineSeparator()).append("      ").append(command.description()));
        return text.toString();
    }
}
