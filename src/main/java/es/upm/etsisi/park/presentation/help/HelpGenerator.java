package es.upm.etsisi.park.presentation.help;

/**
 * Produces help text from the command catalogue using an injected {@link HelpFormatter}.
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
