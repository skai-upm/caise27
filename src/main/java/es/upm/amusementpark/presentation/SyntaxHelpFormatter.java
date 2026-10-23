package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Lists only the syntax of each command (output of HELP).
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder help = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            help.append(System.lineSeparator()).append("  ").append(command.syntax());
        }
        return help.toString();
    }
}
