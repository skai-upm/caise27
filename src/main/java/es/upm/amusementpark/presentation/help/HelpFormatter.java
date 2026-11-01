package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Turns the registered commands into help text. New help formats are added by
 * implementing this interface and injecting the implementation.
 */
@FunctionalInterface
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
