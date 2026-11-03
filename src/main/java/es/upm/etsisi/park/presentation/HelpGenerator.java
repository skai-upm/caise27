package es.upm.etsisi.park.presentation;

import java.util.List;

/**
 * Generates help text with the formatting behavior it receives by injection,
 * independently of how commands are read and executed.
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
