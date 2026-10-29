package es.upm.etsisi.amusementpark.presentation;

/**
 * Produces help text for the command catalogue using the injected format.
 */
public class HelpGenerator {

    private final HelpFormat format;

    public HelpGenerator(HelpFormat format) {
        this.format = format;
    }

    public String generate() {
        return format.format(CommandCatalog.COMMANDS);
    }
}
