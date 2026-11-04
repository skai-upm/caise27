package es.upm.park.presentation;

import java.util.List;

/** Extended help: the syntax of each command followed by a short explanation. */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("OK: available commands (extended):");
        for (CommandInfo c : commands) {
            sb.append(nl).append("  ").append(c.syntax()).append(nl).append("      ").append(c.description());
        }
        return sb.toString();
    }
}
