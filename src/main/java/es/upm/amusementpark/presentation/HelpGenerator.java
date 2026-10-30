package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Generates help text from the command catalog using the formatting behaviour injected at construction.
 */
public class HelpGenerator {

    private final List<CommandHelp> commands;
    private final HelpFormatter formatter;

    public HelpGenerator(List<CommandHelp> commands, HelpFormatter formatter) {
        this.commands = List.copyOf(commands);
        this.formatter = formatter;
    }

    public String generate() {
        return formatter.format(commands);
    }
}
