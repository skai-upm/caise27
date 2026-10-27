package es.upm.amusementpark.presentation.help;

/**
 * Generates help text from the command catalogue using an injected formatting behaviour.
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
