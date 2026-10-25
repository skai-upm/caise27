package es.upm.parque.presentation.help;

import es.upm.parque.presentation.CommandDefinition;

import java.util.List;

/**
 * Strategy that renders the help text for a list of registered commands.
 */
public interface HelpFormatter {

    String format(List<CommandDefinition> commands);
}
