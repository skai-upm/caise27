package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * A way of presenting the list of available commands.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
