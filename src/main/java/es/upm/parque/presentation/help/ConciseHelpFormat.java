package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Lists only the syntax of each command.
 */
public class ConciseHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandDescriptor> commands) {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (CommandDescriptor c : commands) {
            sb.append(System.lineSeparator()).append("  ").append(c.getSyntax());
        }
        return sb.toString();
    }
}
