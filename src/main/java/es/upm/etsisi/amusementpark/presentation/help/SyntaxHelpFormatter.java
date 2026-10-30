package es.upm.etsisi.amusementpark.presentation.help;

import es.upm.etsisi.amusementpark.presentation.command.CommandDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * HELP: one syntax per line.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDescriptor> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        commands.forEach(c -> lines.add(c.syntax()));
        return String.join(System.lineSeparator(), lines);
    }
}
