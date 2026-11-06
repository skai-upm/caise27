package es.upm.park.presentation.help;

import java.util.List;

/**
 * Behavior used to turn the command catalog into help text. New help
 * formats are added by implementing this interface.
 */
@FunctionalInterface
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
