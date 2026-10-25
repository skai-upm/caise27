package es.upm.parque.presentation.help;

import es.upm.parque.presentation.CommandDefinition;

import java.util.List;

/**
 * One line per command with its syntax.
 */
public class ConciseHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDefinition> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        commands.forEach(command -> text.append(System.lineSeparator()).append("  ").append(command.syntax()));
        return text.toString();
    }
}
