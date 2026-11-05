package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Generates help texts using the format injected at construction time.
 */
public class HelpGenerator {

    private final HelpFormat format;

    public HelpGenerator(HelpFormat format) {
        this.format = format;
    }

    public String generate(List<CommandInfo> commands) {
        return format.format(commands);
    }
}
