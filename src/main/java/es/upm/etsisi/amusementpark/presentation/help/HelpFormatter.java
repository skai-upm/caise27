package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

/** A way of presenting the registered commands as help text (strategy). */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
