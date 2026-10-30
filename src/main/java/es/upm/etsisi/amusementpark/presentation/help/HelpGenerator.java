package es.upm.etsisi.amusementpark.presentation.help;

import es.upm.etsisi.amusementpark.presentation.command.CommandDescriptor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Produces help text with the formatting behaviour injected for each help mode.
 */
public class HelpGenerator {

    private final Map<HelpMode, HelpFormatter> formatters;

    public HelpGenerator(Map<HelpMode, HelpFormatter> formatters) {
        this.formatters = new EnumMap<>(formatters);
    }

    public String generate(HelpMode mode, List<CommandDescriptor> commands) {
        HelpFormatter formatter = formatters.get(mode);
        if (formatter == null) {
            throw new IllegalStateException("No help formatter configured for " + mode);
        }
        return formatter.format(commands);
    }
}
