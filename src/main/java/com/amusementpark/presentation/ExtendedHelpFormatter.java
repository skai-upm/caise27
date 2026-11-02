package com.amusementpark.presentation;

import java.util.List;

/**
 * Extended help: each command's syntax followed by a short description of its behavior.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<HelpEntry> entries) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("Available commands (extended):");
        for (HelpEntry e : entries) {
            sb.append(nl).append("  ").append(e.syntax()).append(nl).append("      ").append(e.description());
        }
        return sb.toString();
    }
}
