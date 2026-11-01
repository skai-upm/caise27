package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Turns the registered commands into help text. Each help variant is one implementation.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
