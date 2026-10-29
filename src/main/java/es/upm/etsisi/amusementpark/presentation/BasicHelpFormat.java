package es.upm.etsisi.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Help listing only the syntax of each command.
 */
public class BasicHelpFormat implements HelpFormat {

    @Override
    public String format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        for (CommandInfo command : commands) {
            lines.add(command.syntax());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
