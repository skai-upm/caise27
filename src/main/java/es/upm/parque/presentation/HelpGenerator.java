package es.upm.parque.presentation;

import java.util.List;

/**
 * Builds help text for a list of registered commands. How the text looks is decided by the injected
 * {@link HelpFormatter}, so new formats need no change here or in command processing.
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
