package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

import es.upm.etsisi.amusementpark.presentation.CommandSpec;

/**
 * Generates help text for a command catalog using the injected rendering strategy.
 */
public class HelpGenerator {
    private final HelpStrategy strategy;
    private final List<CommandSpec> commands;

    public HelpGenerator(HelpStrategy strategy, List<CommandSpec> commands) {
        this.strategy = strategy;
        this.commands = List.copyOf(commands);
    }

    public String generate() {
        return strategy.render(commands);
    }
}
