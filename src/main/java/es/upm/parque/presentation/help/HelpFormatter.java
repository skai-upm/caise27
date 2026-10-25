package es.upm.parque.presentation.help;

import java.util.List;

import es.upm.parque.presentation.command.Command;

/**
 * A way of presenting help about commands.
 */
public interface HelpFormatter {

    String format(List<Command> commands);
}
