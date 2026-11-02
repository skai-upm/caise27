package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;
import java.util.stream.Collectors;

import es.upm.etsisi.amusementpark.presentation.CommandSpec;

/**
 * HELP EXTENDED: each command's syntax followed by a short explanation of its purpose.
 */
public class ExtendedHelp implements HelpStrategy {
    @Override
    public String render(List<CommandSpec> commands) {
        String nl = System.lineSeparator();
        return "Available commands:" + nl + commands.stream()
                .map(c -> "  " + c.syntax() + nl + "      " + c.description())
                .collect(Collectors.joining(nl));
    }
}
