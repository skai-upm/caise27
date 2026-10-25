package es.upm.parque.presentation.help;

import es.upm.parque.presentation.CommandDefinition;

import java.util.List;
import java.util.Map;

/**
 * Builds help texts. The formatting behavior for each help mode is injected, so new presentation formats
 * can be added without touching command processing.
 */
public class HelpGenerator {

    private final Map<String, HelpFormatter> formatters;

    public HelpGenerator(Map<String, HelpFormatter> formatters) {
        this.formatters = Map.copyOf(formatters);
    }

    /**
     * Renders the help for the given mode, or null if no formatter is registered for it.
     */
    public String generate(String mode, List<CommandDefinition> commands) {
        HelpFormatter formatter = formatters.get(mode);
        return formatter == null ? null : formatter.format(commands);
    }
}
