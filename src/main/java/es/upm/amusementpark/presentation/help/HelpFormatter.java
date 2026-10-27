package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Behaviour that turns the command catalogue into help text.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
