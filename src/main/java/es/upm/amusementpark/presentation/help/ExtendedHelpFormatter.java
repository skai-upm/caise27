package es.upm.amusementpark.presentation.help;

import es.upm.amusementpark.presentation.command.CommandDefinition;

import java.util.List;

/**
 * Lists the syntax of every command followed by a brief description of its behavior.
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDefinition> commands) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("Available commands (extended):");
        for (CommandDefinition command : commands) {
            for (String syntax : command.syntaxes()) {
                sb.append(nl).append("  ").append(syntax);
            }
            sb.append(nl).append("      ").append(command.description());
        }
        return sb.toString();
    }
}
