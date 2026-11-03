package es.upm.etsisi.amusementpark.presentation;

/**
 * Generates help text from a command catalogue using an injected format.
 */
public class HelpGenerator {

    private final CommandCatalog catalog;
    private final HelpFormat format;

    public HelpGenerator(CommandCatalog catalog, HelpFormat format) {
        this.catalog = catalog;
        this.format = format;
    }

    public String generate() {
        return format.format(catalog.getCommands());
    }
}
