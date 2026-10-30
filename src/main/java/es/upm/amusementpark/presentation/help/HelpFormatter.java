package es.upm.amusementpark.presentation.help;

import es.upm.amusementpark.presentation.command.CommandDefinition;

import java.util.List;

/**
 * Strategy that turns the registered commands into help text.
 */
public interface HelpFormatter {

    String format(List<CommandDefinition> commands);
}
