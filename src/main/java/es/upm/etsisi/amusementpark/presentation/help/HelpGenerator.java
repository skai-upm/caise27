package es.upm.etsisi.amusementpark.presentation.help;

import es.upm.etsisi.amusementpark.presentation.CommandRegistry;

/**
 * Produces help text for the registered commands with an injected formatting strategy.
 */
public class HelpGenerator {

    private final CommandRegistry registry;
    private final HelpFormatter formatter;

    public HelpGenerator(CommandRegistry registry, HelpFormatter formatter) {
        this.registry = registry;
        this.formatter = formatter;
    }

    public String generate() {
        return formatter.format(registry.specs());
    }
}
