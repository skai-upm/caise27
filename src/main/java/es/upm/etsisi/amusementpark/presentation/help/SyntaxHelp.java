package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;
import java.util.stream.Collectors;

import es.upm.etsisi.amusementpark.presentation.CommandSpec;

/**
 * HELP: one line with the syntax of each command.
 */
public class SyntaxHelp implements HelpStrategy {
    @Override
    public String render(List<CommandSpec> commands) {
        return "Available commands:" + System.lineSeparator() + commands.stream()
                .map(c -> "  " + c.syntax())
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
