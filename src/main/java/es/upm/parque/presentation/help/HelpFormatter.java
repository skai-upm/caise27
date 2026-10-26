package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Presentation format of the help text.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
