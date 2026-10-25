package es.upm.parque.presentation.help;

import es.upm.parque.presentation.CommandDefinition;

import java.util.List;

/**
 * Strategy that turns the registered commands into help text.
 */
public interface HelpFormatter {

    String format(List<CommandDefinition> commands);
}
