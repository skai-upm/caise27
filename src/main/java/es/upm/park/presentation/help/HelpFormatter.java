package es.upm.park.presentation.help;

import java.util.List;

/**
 * Strategy that turns the command descriptions into help text. New help
 * styles are added by implementing this interface.
 */
public interface HelpFormatter {

    String format(List<CommandHelp> commands);
}
