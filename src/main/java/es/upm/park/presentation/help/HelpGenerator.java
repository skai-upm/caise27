package es.upm.park.presentation.help;

import java.util.List;

/**
 * Generates help text for a command catalog using the formatting behavior
 * injected at construction.
 */
public class HelpGenerator {

    private final HelpFormatter formatter;
    private final List<CommandInfo> commands;

    public HelpGenerator(HelpFormatter formatter, List<CommandInfo> commands) {
        this.formatter = formatter;
        this.commands = List.copyOf(commands);
    }

    public String generate() {
        return formatter.format(commands);
    }
}
