package es.upm.park.presentation.help;

import java.util.List;

/** Extended help: each syntax line followed by the command's purpose. */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDescription> commands) {
        String nl = System.lineSeparator();
        StringBuilder out = new StringBuilder("Available commands (extended):");
        for (CommandDescription command : commands) {
            out.append(nl).append(command.syntax()).append(nl).append("    ").append(command.purpose());
        }
        return out.toString();
    }
}
