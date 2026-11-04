package com.parque.presentation;

import java.util.List;

/**
 * Concise help: the syntax of each command.
 */
public class SyntaxHelpFormatter implements HelpFormatter {
    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (CommandInfo c : commands) {
            sb.append(System.lineSeparator()).append("  ").append(c.syntax());
        }
        return sb.toString();
    }
}
