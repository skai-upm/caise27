package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Strategy that turns the command catalog into help text.
 */
public interface HelpFormatter {

    String format(List<CommandHelp> commands);
}
