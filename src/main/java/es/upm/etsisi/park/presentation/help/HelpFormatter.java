package es.upm.etsisi.park.presentation.help;

import java.util.List;

/**
 * A way of presenting the command list. New help styles implement this interface and are
 * injected; command processing does not change.
 */
public interface HelpFormatter {

    List<String> format(List<CommandInfo> commands);
}
