package es.upm.etsisi.park.presentation.help;

import java.util.List;

/**
 * Strategy that turns the command catalogue into help text.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
