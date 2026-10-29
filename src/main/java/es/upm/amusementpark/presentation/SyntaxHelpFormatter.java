package es.upm.amusementpark.presentation;

import java.util.List;

/** Help listing only the syntax of each command. */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            text.append("\n  ").append(command.syntax());
        }
        return text.toString();
    }
}
