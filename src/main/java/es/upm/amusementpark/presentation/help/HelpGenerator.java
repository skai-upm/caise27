package es.upm.amusementpark.presentation.help;

import es.upm.amusementpark.presentation.command.CommandRegistry;

/**
 * Builds the help text from the registered commands using the formatters injected at startup.
 */
public class HelpGenerator {

    private final CommandRegistry registry;
    private final HelpFormatter conciseFormatter;
    private final HelpFormatter extendedFormatter;

    public HelpGenerator(CommandRegistry registry, HelpFormatter conciseFormatter, HelpFormatter extendedFormatter) {
        this.registry = registry;
        this.conciseFormatter = conciseFormatter;
        this.extendedFormatter = extendedFormatter;
    }

    public String concise() {
        return conciseFormatter.format(registry.all());
    }

    public String extended() {
        return extendedFormatter.format(registry.all());
    }
}
