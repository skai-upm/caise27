package es.upm.park.presentation;

import java.util.List;

/** Concise help: the syntax of every command, one per line. */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            text.append(System.lineSeparator()).append(command.syntax());
        }
        return text.toString();
    }
}
