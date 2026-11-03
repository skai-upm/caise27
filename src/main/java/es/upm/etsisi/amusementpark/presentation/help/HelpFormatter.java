package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

/** Behaviour that turns the registered commands into help text; new presentation formats implement it. */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
