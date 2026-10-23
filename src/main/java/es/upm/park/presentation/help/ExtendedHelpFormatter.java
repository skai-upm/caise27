package es.upm.park.presentation.help;

import java.util.List;

/**
 * HELP EXTENDED: each command's syntax followed by what it is for.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandHelp> commands) {
        StringBuilder help = new StringBuilder("Available commands (extended):");
        for (CommandHelp command : commands) {
            help.append(System.lineSeparator()).append("  ").append(command.syntax())
                    .append(System.lineSeparator()).append("      ").append(command.description());
        }
        return help.toString();
    }
}
