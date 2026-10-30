package es.upm.amusementpark.presentation.help;

import es.upm.amusementpark.presentation.command.CommandDefinition;

import java.util.List;

/**
 * Lists the syntax of every command.
 */
public class ConciseHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDefinition> commands) {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (CommandDefinition command : commands) {
            for (String syntax : command.syntaxes()) {
                sb.append(System.lineSeparator()).append("  ").append(syntax);
            }
        }
        return sb.toString();
    }
}
