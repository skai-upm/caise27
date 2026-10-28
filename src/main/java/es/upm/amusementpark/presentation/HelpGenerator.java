package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Generates help text; the way the text is built is injected as a {@link HelpFormat}.
 */
public class HelpGenerator {

    private final List<CommandDescriptor> commands;
    private final HelpFormat format;

    public HelpGenerator(List<CommandDescriptor> commands, HelpFormat format) {
        this.commands = List.copyOf(commands);
        this.format = format;
    }

    public String generate() {
        return format.format(commands);
    }
}
