package es.upm.etsisi.amusementpark.presentation.help;

import es.upm.etsisi.amusementpark.presentation.command.CommandDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * HELP EXTENDED: each syntax followed by a description of its purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDescriptor> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands (extended):");
        for (CommandDescriptor c : commands) {
            lines.add(c.syntax());
            lines.add("    " + c.description());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
