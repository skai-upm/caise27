package es.upm.park.presentation;

import java.util.List;

/**
 * Builds help text from the registered commands with an injected {@link HelpFormatter};
 * new help formats only need a new formatter.
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
