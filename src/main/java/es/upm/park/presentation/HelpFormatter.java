package es.upm.park.presentation;

import java.util.List;

/** Behaviour that turns the registered commands into a help text. */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
