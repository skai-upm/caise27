package com.amusementpark.presentation.help;

import java.util.List;

/**
 * Extended help: each syntax followed by its description.
 */
public class ExtendedHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax())
                    .append(System.lineSeparator()).append("      ").append(command.description());
        }
        return text.toString();
    }
}
