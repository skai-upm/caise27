package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Generates help text for the command catalog using the formatting behaviour injected at construction.
 */
public class HelpGenerator {

    private final List<CommandInfo> commands;
    private final HelpFormatter formatter;

    public HelpGenerator(List<CommandInfo> commands, HelpFormatter formatter) {
        this.commands = List.copyOf(commands);
        this.formatter = formatter;
    }

    public String generate() {
        return formatter.format(commands);
    }
}
