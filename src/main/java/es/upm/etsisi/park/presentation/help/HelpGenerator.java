package es.upm.etsisi.park.presentation.help;

import java.util.List;

/**
 * Generates help text with the formatting behavior it is given.
 */
public class HelpGenerator {

    private final HelpFormatter formatter;

    public HelpGenerator(HelpFormatter formatter) {
        this.formatter = formatter;
    }

    public List<String> generate(List<CommandInfo> commands) {
        return formatter.format(commands);
    }
}
