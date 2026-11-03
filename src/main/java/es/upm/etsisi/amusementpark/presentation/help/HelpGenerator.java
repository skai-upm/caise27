package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

/**
 * Builds help text from the registered commands. The formatting behaviour is injected through the
 * constructor, so new formats can be added without changing this class or the command processing.
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
