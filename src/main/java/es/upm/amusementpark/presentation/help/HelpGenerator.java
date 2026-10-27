package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Generates help text for the application's commands. The way the text is
 * built is the {@link HelpFormatter} injected at construction.
 */
public class HelpGenerator {

    private final HelpFormatter formatter;
    private final List<CommandInfo> commands;

    public HelpGenerator(HelpFormatter formatter) {
        this(formatter, CommandCatalog.commands());
    }

    public HelpGenerator(HelpFormatter formatter, List<CommandInfo> commands) {
        this.formatter = formatter;
        this.commands = List.copyOf(commands);
    }

    public String generate() {
        return String.join(System.lineSeparator(), formatter.format(commands));
    }
}
