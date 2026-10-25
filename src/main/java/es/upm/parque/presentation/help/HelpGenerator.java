package es.upm.parque.presentation.help;

import es.upm.parque.presentation.CommandRegistry;

/**
 * Builds help text from the registered commands using the formatting behaviour it is given.
 */
public class HelpGenerator {

    private final HelpFormatter formatter;

    public HelpGenerator(HelpFormatter formatter) {
        this.formatter = formatter;
    }

    public String generate(CommandRegistry registry) {
        return formatter.format(registry.definitions());
    }
}
