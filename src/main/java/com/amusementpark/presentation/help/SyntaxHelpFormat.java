package com.amusementpark.presentation.help;

import java.util.List;

/**
 * Concise help: one syntax per line.
 */
public class SyntaxHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax());
        }
        return text.toString();
    }
}
