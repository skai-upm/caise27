package com.parque.presentation;

import java.util.List;

/**
 * Extended help: the syntax of each command followed by a brief description of its behaviour.
 */
public class ExtendedHelpFormatter implements HelpFormatter {
    @Override
    public String format(List<CommandInfo> commands) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("Available commands (extended):");
        for (CommandInfo c : commands) {
            sb.append(nl).append("  ").append(c.syntax());
            sb.append(nl).append("      ").append(c.description());
        }
        return sb.toString();
    }
}
