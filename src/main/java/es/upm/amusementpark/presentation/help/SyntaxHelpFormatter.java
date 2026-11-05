package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Plain help: command syntax only.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDescription> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandDescription command : commands) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax());
        }
        return text.toString();
    }
}
