package es.upm.amusementpark.presentation;

import java.util.List;
import java.util.Map;

/**
 * Produces help text for a mode (e.g. {@code ""} for HELP, {@code "EXTENDED"} for
 * HELP EXTENDED) using the formatter injected for that mode.
 */
public class HelpGenerator {

    private final List<CommandInfo> commands;
    private final Map<String, HelpFormatter> formatters;

    public HelpGenerator(List<CommandInfo> commands, Map<String, HelpFormatter> formatters) {
        this.commands = List.copyOf(commands);
        this.formatters = Map.copyOf(formatters);
    }

    public boolean supports(String mode) {
        return formatters.containsKey(mode);
    }

    public String generate(String mode) {
        HelpFormatter formatter = formatters.get(mode);
        if (formatter == null) {
            throw new IllegalArgumentException("No help formatter for mode '" + mode + "'");
        }
        return formatter.format(commands);
    }
}
