package es.upm.etsisi.amusementpark.presentation.help;

/**
 * Builds the help text from the registered commands. The formatting behaviour
 * of each help mode and the command registry are injected, never created here.
 */
public class HelpGenerator {

    private final CommandCatalog catalog;
    private final HelpFormatter conciseFormatter;
    private final HelpFormatter extendedFormatter;

    public HelpGenerator(CommandCatalog catalog, HelpFormatter conciseFormatter, HelpFormatter extendedFormatter) {
        this.catalog = catalog;
        this.conciseFormatter = conciseFormatter;
        this.extendedFormatter = extendedFormatter;
    }

    /** Text for {@code HELP}. */
    public String concise() {
        return conciseFormatter.format(catalog.commands());
    }

    /** Text for {@code HELP EXTENDED}. */
    public String extended() {
        return extendedFormatter.format(catalog.commands());
    }
}
