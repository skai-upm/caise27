package es.upm.park.presentation;

import java.util.List;

/** Extended help: the syntax of every command followed by an indented explanation. */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandInfo command : commands) {
            text.append(nl).append(command.syntax()).append(nl).append("    ").append(command.description());
        }
        return text.toString();
    }
}
