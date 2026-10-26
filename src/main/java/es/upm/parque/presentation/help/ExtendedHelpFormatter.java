package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Extended help: the syntax of each command followed by an indented description of its behaviour.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        String nl = System.lineSeparator();
        return String.join(nl, commands.stream().map(c -> c.syntax() + nl + "    " + c.description()).toList());
    }
}
