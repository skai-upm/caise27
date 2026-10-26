package es.upm.parque.presentation;

import java.util.List;

/**
 * Extended help: the syntax of each command followed by a brief description of its behavior.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            text.append(nl).append("  ").append(command.syntax())
                .append(nl).append("      ").append(command.description());
        }
        return text.toString();
    }
}
