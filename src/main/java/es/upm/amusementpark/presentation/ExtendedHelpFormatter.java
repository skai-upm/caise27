package es.upm.amusementpark.presentation;

import java.util.List;

/** Help listing each command's syntax followed by a short explanation of its purpose. */
public class ExtendedHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandInfo command : commands) {
            text.append("\n  ").append(command.syntax()).append("\n      ").append(command.description());
        }
        return text.toString();
    }
}
