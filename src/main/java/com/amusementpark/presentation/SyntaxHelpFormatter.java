package com.amusementpark.presentation;

import java.util.List;

/**
 * Concise help: one syntax line per command.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<HelpEntry> entries) {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (HelpEntry e : entries) {
            sb.append(System.lineSeparator()).append("  ").append(e.syntax());
        }
        return sb.toString();
    }
}
