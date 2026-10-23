package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Produces help text for a list of commands. How the text is laid out is
 * decided by the {@link HelpFormatter} injected when the generator is
 * created, so new help styles need no change in command processing.
 */
public class HelpGenerator {

    private final HelpFormatter formatter;

    public HelpGenerator(HelpFormatter formatter) {
        this.formatter = formatter;
    }

    public String generate(List<CommandInfo> commands) {
        return formatter.format(commands);
    }
}
