package es.upm.etsisi.park.presentation.help;

import java.util.List;

/**
 * Lists each command's syntax followed by its purpose (HELP EXTENDED).
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandInfo command : commands) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax())
                    .append(System.lineSeparator()).append("      ").append(command.description());
        }
        return text.toString();
    }
}
