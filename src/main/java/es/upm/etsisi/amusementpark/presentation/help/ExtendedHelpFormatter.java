package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

/** Extended help: the syntax of every command followed by a brief description of its behaviour. */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandInfo c : commands) {
            text.append(nl).append("  ").append(c.getSyntax()).append(nl).append("      ").append(c.getDescription());
        }
        return text.toString();
    }
}
