package es.upm.etsisi.amusementpark.presentation.help;

import es.upm.etsisi.amusementpark.presentation.CommandSpec;
import java.util.List;

/**
 * Strategy that turns the registered commands into help text.
 */
public interface HelpFormatter {

    String format(List<CommandSpec> commands);
}
