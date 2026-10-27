package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * HELP: the syntax of every command.
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder out = new StringBuilder("Available commands:");
        for (CommandInfo command : commands) {
            out.append('\n').append(command.syntax());
        }
        return out.toString();
    }
}
