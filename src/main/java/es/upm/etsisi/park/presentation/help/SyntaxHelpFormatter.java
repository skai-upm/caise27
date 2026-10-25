package es.upm.etsisi.park.presentation.help;

import java.util.List;

/**
 * Lists only the syntax of each command (HELP).
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax());
        }
        return text.toString();
    }
}
