package es.upm.parque.presentation;

import java.util.List;
import java.util.Objects;

/**
 * Generates help text. The behaviour that builds the output is injected as a {@link HelpFormatter}, so new help
 * variants need no change here.
 */
public class HelpGenerator {

    private final List<CommandDescriptor> commands;
    private final HelpFormatter formatter;

    public HelpGenerator(List<CommandDescriptor> commands, HelpFormatter formatter) {
        this.commands = List.copyOf(commands);
        this.formatter = Objects.requireNonNull(formatter);
    }

    public String generate() {
        return formatter.format(commands);
    }
}
