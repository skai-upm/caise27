package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * HELP EXTENDED: the syntax of each command followed by a brief explanation.
 */
public class ExtendedHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandCatalog.CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandCatalog.CommandInfo command : commands) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax())
                    .append(System.lineSeparator()).append("      ").append(command.purpose());
        }
        return text.toString();
    }
}
