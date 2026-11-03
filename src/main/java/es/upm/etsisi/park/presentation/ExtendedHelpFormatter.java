package es.upm.etsisi.park.presentation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HELP EXTENDED: each syntax line followed by a brief explanation of its purpose.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        return commands.stream()
                .map(c -> c.syntax() + System.lineSeparator() + "    " + c.purpose())
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
