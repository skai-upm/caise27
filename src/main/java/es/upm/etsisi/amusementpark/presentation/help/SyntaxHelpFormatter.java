package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

/** Concise help: one syntax line per command. */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder sb = new StringBuilder("OK: Available commands:");
        for (CommandInfo c : commands) {
            sb.append(System.lineSeparator()).append("  ").append(c.syntax());
        }
        return sb.toString();
    }
}
