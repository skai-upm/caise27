package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Presentation behaviour used to turn the registered commands into help text.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
