package es.upm.amusementpark.presentation.help;

/**
 * Generates help text from the command catalog using the formatting behaviour it is given.
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
