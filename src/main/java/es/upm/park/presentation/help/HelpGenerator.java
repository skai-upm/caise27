package es.upm.park.presentation.help;

import java.util.List;

/**
 * Generates help text for a set of commands using the formatting behavior it
 * is given. New help formats only need a new {@link HelpFormatter}.
 */
public class HelpGenerator {

    private final List<CommandDescription> commands;
    private final HelpFormatter formatter;

    public HelpGenerator(List<CommandDescription> commands, HelpFormatter formatter) {
        this.commands = List.copyOf(commands);
        this.formatter = formatter;
    }

    public String generate() {
        return formatter.format(commands);
    }
}
