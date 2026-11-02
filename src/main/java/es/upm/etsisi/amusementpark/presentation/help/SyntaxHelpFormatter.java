package es.upm.etsisi.amusementpark.presentation.help;

import es.upm.etsisi.amusementpark.presentation.CommandSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * One line per command syntax.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandSpec> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        commands.forEach(c -> lines.addAll(c.syntaxes()));
        return String.join(System.lineSeparator(), lines);
    }
}
