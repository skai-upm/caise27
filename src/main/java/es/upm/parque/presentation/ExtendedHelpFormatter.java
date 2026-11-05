package es.upm.parque.presentation;

import java.util.List;

/**
 * Help text with the syntax of each command followed by a brief explanation of its purpose (HELP EXTENDED).
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    private static final String NL = System.lineSeparator();

    @Override
    public String format(List<CommandDescriptor> commands) {
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandDescriptor command : commands) {
            text.append(NL).append("  ").append(command.syntax())
                    .append(NL).append("      ").append(command.purpose());
        }
        return text.toString();
    }
}
