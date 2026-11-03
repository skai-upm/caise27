package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Plain HELP: one line with the syntax of each command.
 */
public class SyntaxHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandCatalog.CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandCatalog.CommandInfo command : commands) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax());
        }
        return text.toString();
    }
}
