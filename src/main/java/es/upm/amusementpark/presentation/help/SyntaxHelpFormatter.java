package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Basic help: one syntax line per command.
 */
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
