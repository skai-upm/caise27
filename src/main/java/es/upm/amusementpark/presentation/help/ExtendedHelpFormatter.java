package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Extended help: command syntax followed by a brief explanation of its purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDescription> commands) {
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandDescription command : commands) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax())
                    .append(System.lineSeparator()).append("      ").append(command.purpose());
        }
        return text.toString();
    }
}
