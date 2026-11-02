package es.upm.etsisi.park.presentation;

import java.util.Collection;
import java.util.List;

/**
 * Generates help text with the formatting behavior injected at construction.
 */
public class HelpGenerator {

    private final HelpFormatter formatter;

    public HelpGenerator(HelpFormatter formatter) {
        this.formatter = formatter;
    }

    public String generate(Collection<CommandDefinition> commands) {
        return formatter.format(List.copyOf(commands));
    }
}
