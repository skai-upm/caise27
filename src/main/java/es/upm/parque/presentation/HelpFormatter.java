package es.upm.parque.presentation;

import java.util.List;

/**
 * Behavior that renders the registered commands as help text. New help formats implement this interface.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
