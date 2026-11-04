package com.parque.presentation;

/**
 * Builds help text from the registered commands using the formatting behaviour injected at construction.
 * It does not create its dependencies and knows nothing about command parsing or execution.
 */
public class HelpGenerator {
    private final CommandCatalog catalog;
    private final HelpFormatter formatter;

    public HelpGenerator(CommandCatalog catalog, HelpFormatter formatter) {
        this.catalog = catalog;
        this.formatter = formatter;
    }

    public String generate() {
        return formatter.format(catalog.commands());
    }
}
