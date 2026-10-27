package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * HELP EXTENDED: the syntax of every command followed by its purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder out = new StringBuilder("Available commands (extended):");
        for (CommandInfo command : commands) {
            out.append('\n').append(command.syntax()).append("\n    ").append(command.purpose());
        }
        return out.toString();
    }
}
