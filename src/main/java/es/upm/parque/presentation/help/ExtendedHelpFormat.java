package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Extended help: syntax followed by a description of each command.
 */
public class ExtendedHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandInfo c : commands) {
            text.append(System.lineSeparator()).append("  ").append(c.syntax())
                    .append(System.lineSeparator()).append("      ").append(c.description());
        }
        return text.toString();
    }
}
