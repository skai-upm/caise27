package es.upm.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Syntax of each command, one per line.
 */
public class BasicHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandDescriptor> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        commands.forEach(c -> lines.add(c.syntax()));
        lines.add("(<Field>: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
        return String.join(System.lineSeparator(), lines);
    }
}
