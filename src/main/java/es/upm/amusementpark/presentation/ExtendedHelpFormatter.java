package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Lists the syntax of each command followed by an explanation of its purpose
 * (output of HELP EXTENDED).
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder help = new StringBuilder("Available commands (extended):");
        for (CommandInfo command : commands) {
            help.append(System.lineSeparator()).append("  ").append(command.syntax())
                    .append(System.lineSeparator()).append("      ").append(command.description());
        }
        return help.toString();
    }
}
