package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Builds help text for the registered commands using the injected format.
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
