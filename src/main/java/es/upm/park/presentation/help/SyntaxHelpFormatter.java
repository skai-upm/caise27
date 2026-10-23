package es.upm.park.presentation.help;

import java.util.List;

/**
 * HELP: one syntax line per command.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandHelp> commands) {
        StringBuilder help = new StringBuilder("Available commands:");
        for (CommandHelp command : commands) {
            help.append(System.lineSeparator()).append("  ").append(command.syntax());
        }
        return help.toString();
    }
}
