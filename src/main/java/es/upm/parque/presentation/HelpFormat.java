package es.upm.parque.presentation;

import java.util.List;

/**
 * Presentation behavior used to turn the registered commands into a help text.
 */
public interface HelpFormat {

    String format(List<CommandInfo> commands);
}
