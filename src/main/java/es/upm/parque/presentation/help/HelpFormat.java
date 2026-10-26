package es.upm.parque.presentation.help;

import java.util.List;

/**
 * A way of presenting the registered commands as help text.
 */
public interface HelpFormat {

    String format(List<CommandDescriptor> commands);
}
