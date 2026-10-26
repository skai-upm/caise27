package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Builds the help text of the registered commands with the presentation behaviour it is given.
 * Both the commands and the behaviour are injected; nothing is created here.
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
