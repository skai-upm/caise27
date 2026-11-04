package es.upm.park.presentation;

import java.util.List;

/** Concise help: the syntax of each command and the editable user fields. */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("OK: available commands:");
        for (CommandInfo c : commands) {
            sb.append(nl).append("  ").append(c.syntax());
        }
        return sb.append(nl).append("  Campo (USER UPDATE): ").append(CommandCatalog.fieldNames()).toString();
    }
}
