package es.upm.amusementpark.presentation.help;

/**
 * Builds help text from the command registry. The output format is the
 * injected {@link HelpFormatter}, so new formats need no change here or in
 * command processing.
 */
public class HelpGenerator {

    private final CommandRegistry registry;
    private final HelpFormatter formatter;

    public HelpGenerator(CommandRegistry registry, HelpFormatter formatter) {
        this.registry = registry;
        this.formatter = formatter;
    }

    public String generate() {
        return formatter.format(registry.getCommands());
    }
}
