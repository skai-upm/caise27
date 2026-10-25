package es.upm.parque.presentation.help;

import es.upm.parque.presentation.command.CommandRegistry;

/**
 * Produces help text about the registered commands using the injected presentation format.
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
