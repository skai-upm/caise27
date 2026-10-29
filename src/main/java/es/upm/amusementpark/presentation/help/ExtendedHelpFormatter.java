package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Extended help: each command's syntax followed by an indented description of its purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandInfo command : commands) {
            text.append(System.lineSeparator()).append(command.syntax())
                    .append(System.lineSeparator()).append("    ").append(command.description());
        }
        return text.toString();
    }
}
