package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Strategy that renders help text for a list of commands.
 */
public interface HelpFormat {

    String format(List<CommandInfo> commands);
}
