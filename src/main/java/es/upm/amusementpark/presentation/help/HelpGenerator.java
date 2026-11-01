package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Produces help text for the registered commands using the injected formatter.
 */
public class HelpGenerator {

    private final List<CommandInfo> commands;
    private final HelpFormatter formatter;

    public HelpGenerator(List<CommandInfo> commands, HelpFormatter formatter) {
        this.commands = List.copyOf(commands);
        this.formatter = formatter;
    }

    public String generate() {
        return formatter.format(commands);
    }
}
