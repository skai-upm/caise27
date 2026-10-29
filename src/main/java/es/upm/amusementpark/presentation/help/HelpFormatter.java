package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Strategy that turns the command catalog into help text.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
