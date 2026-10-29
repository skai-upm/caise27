package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Help format listing each command's syntax followed by a brief explanation (HELP EXTENDED).
 */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("Available commands (extended):");
        for (CommandInfo command : commands) {
            sb.append(nl).append(command.syntax()).append(nl).append("    ").append(command.description());
        }
        return sb.toString();
    }
}
