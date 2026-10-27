package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * A way of presenting the help for a list of commands.
 */
public interface HelpFormatter {

    List<String> format(List<CommandInfo> commands);
}
