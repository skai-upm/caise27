package es.upm.parque.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended help: each syntax followed by an indented description of the command's behavior.
 */
public class ExtendedHelpFormat implements HelpFormat {

    private static final String INDENT = "    ";

    @Override
    public String format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        for (CommandInfo command : commands) {
            lines.add(command.getSyntax());
            lines.add(INDENT + command.getDescription());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
