package es.upm.etsisi.park.presentation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plain HELP: one syntax line per command form.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        return commands.stream().map(CommandInfo::syntax).collect(Collectors.joining(System.lineSeparator()));
    }
}
