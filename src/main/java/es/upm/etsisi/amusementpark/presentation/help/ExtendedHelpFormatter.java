package es.upm.etsisi.amusementpark.presentation.help;

import es.upm.etsisi.amusementpark.presentation.CommandSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Each command's syntax followed by a short explanation of its purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandSpec> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandSpec command : commands) {
            lines.addAll(command.syntaxes());
            lines.add("    " + command.description());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
