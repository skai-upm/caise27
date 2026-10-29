package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Help format listing only the syntax of each command (HELP).
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            sb.append(System.lineSeparator()).append(command.syntax());
        }
        return sb.toString();
    }
}
