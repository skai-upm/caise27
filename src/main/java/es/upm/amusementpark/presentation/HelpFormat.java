package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Behavior that turns the command catalog into help text.
 */
@FunctionalInterface
public interface HelpFormat {

    String format(List<CommandDescriptor> commands);
}
