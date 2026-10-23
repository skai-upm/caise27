package es.upm.park.presentation.help;

import java.util.List;

/**
 * Generates help text. How the text is built is injected as a
 * {@link HelpFormatter}, so help variants are independent from command
 * reading and execution.
 */
public class HelpGenerator {

    private final HelpFormatter formatter;

    public HelpGenerator(HelpFormatter formatter) {
        this.formatter = formatter;
    }

    public String generate(List<CommandHelp> commands) {
        return formatter.format(commands);
    }
}
