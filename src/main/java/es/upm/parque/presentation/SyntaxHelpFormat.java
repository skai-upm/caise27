package es.upm.parque.presentation;

import java.util.List;

/**
 * Concise help: one command syntax per line.
 */
public class SyntaxHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandInfo> commands) {
        return String.join(System.lineSeparator(), commands.stream().map(CommandInfo::getSyntax).toList());
    }
}
