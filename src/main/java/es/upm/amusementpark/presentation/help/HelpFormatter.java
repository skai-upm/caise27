package es.upm.amusementpark.presentation.help;

import java.util.List;

/** Output format used to present the registered commands. */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
