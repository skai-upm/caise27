package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Concise help: the syntax of each command, one per line.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        return String.join(System.lineSeparator(), commands.stream().map(CommandInfo::syntax).toList());
    }
}
