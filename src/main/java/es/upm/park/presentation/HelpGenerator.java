package es.upm.park.presentation;

/**
 * Builds help text from the registered commands with the formatting behavior it receives
 * through its constructor.
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
