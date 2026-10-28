package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Behaviour used to build help text from the command help entries.
 */
public interface HelpStrategy {

    String render(List<CommandHelp> entries);
}
