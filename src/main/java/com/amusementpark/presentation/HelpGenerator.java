package com.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds help text for the registered commands using the formatter it was configured with.
 */
public class HelpGenerator {

    private final HelpFormatter formatter;

    public HelpGenerator(HelpFormatter formatter) {
        this.formatter = formatter;
    }

    public String generate(List<CommandDefinition> commands) {
        List<HelpEntry> entries = new ArrayList<>();
        for (CommandDefinition c : commands) {
            entries.add(c.helpEntry());
        }
        return formatter.format(entries);
    }
}
