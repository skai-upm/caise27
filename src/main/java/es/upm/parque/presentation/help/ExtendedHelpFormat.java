package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Lists the syntax of each command followed by a brief description of its behaviour.
 */
public class ExtendedHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandDescriptor> commands) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("Available commands (extended):");
        for (CommandDescriptor c : commands) {
            sb.append(nl).append("  ").append(c.getSyntax()).append(nl).append("      ").append(c.getDescription());
        }
        return sb.toString();
    }
}
