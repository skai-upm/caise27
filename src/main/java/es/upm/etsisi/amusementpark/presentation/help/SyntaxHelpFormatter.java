package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

/** Concise help: the syntax of every command. */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        for (CommandInfo c : commands) {
            text.append(System.lineSeparator()).append("  ").append(c.getSyntax());
        }
        return text.toString();
    }
}
