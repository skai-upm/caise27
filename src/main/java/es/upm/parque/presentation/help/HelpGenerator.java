package es.upm.parque.presentation.help;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Builds help text from the registered commands with the formatter injected for each help mode.
 */
public class HelpGenerator {

    private final CommandCatalog catalog;
    private final Map<String, HelpFormatter> formatters;

    /**
     * @param formatters formatter per help mode; the empty mode is plain {@code HELP}
     */
    public HelpGenerator(CommandCatalog catalog, Map<String, HelpFormatter> formatters) {
        this.catalog = catalog;
        this.formatters = new LinkedHashMap<>(formatters);
    }

    public CommandCatalog getCatalog() {
        return catalog;
    }

    public Set<String> modes() {
        return formatters.keySet();
    }

    /**
     * Returns the help text for the mode, or {@code null} if no formatter is registered for it.
     */
    public String generate(String mode) {
        HelpFormatter formatter = formatters.get(mode);
        return formatter == null ? null : formatter.format(catalog.getCommands());
    }
}
