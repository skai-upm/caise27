package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Behavior used to build a help text from the available commands (strategy).
 */
@FunctionalInterface
public interface HelpFormat {

    String format(List<CommandInfo> commands);
}
