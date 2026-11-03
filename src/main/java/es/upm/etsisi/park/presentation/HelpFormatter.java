package es.upm.etsisi.park.presentation;

import java.util.List;

/**
 * Strategy that turns the registered commands into help text.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
