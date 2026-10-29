package es.upm.amusementpark.presentation;

import java.util.Objects;

/** Generates help text with the formatting behavior injected at construction. */
public class HelpGenerator {

    private final CommandCatalog catalog;
    private final HelpFormatter formatter;

    public HelpGenerator(CommandCatalog catalog, HelpFormatter formatter) {
        this.catalog = Objects.requireNonNull(catalog);
        this.formatter = Objects.requireNonNull(formatter);
    }

    public String generate() {
        return formatter.format(catalog.commands());
    }
}
