package es.upm.parque.presentation;

/**
 * Builds a help text from the registered commands using the injected presentation format.
 */
public class HelpGenerator {

    private final CommandRegistry registry;
    private final HelpFormat format;

    public HelpGenerator(CommandRegistry registry, HelpFormat format) {
        this.registry = registry;
        this.format = format;
    }

    public String generate() {
        return format.format(registry.getCommands());
    }
}
