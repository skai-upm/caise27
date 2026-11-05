package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Behavior used to build help text, injected into {@link HelpGenerator}.
 */
@FunctionalInterface
public interface HelpFormatter {

    String format(List<CommandDescription> commands);
}
