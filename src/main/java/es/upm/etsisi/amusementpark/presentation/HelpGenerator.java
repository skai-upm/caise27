package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Generates help text with the formatting behaviour injected at construction.
 */
public class HelpGenerator {

    private final HelpStrategy strategy;
    private final List<CommandHelp> entries;

    public HelpGenerator(HelpStrategy strategy, List<CommandHelp> entries) {
        this.strategy = strategy;
        this.entries = List.copyOf(entries);
    }

    public String generate() {
        return strategy.render(entries);
    }
}
