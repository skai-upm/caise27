package es.upm.parque.presentation.help;

/**
 * Builds help text from the command catalog using the format it was given.
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
