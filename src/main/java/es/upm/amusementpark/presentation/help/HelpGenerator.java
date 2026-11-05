package es.upm.amusementpark.presentation.help;

import java.util.Objects;

/**
 * Generates help text from a command catalog using an injected formatting behavior.
 */
public class HelpGenerator {

    private final HelpFormatter formatter;
    private final CommandCatalog catalog;

    public HelpGenerator(HelpFormatter formatter, CommandCatalog catalog) {
        this.formatter = Objects.requireNonNull(formatter, "formatter");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    public String generate() {
        return formatter.format(catalog.commands());
    }
}
