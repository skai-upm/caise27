package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

/** Extended help: the syntax of each command followed by a brief description. */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder sb = new StringBuilder("OK: Available commands (extended):");
        for (CommandInfo c : commands) {
            sb.append(System.lineSeparator()).append("  ").append(c.syntax())
                    .append(System.lineSeparator()).append("      ").append(c.description());
        }
        return sb.toString();
    }
}
