package es.upm.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Help listing only the syntax of each command.
 */
public class ConciseHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandInfo> commands) {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        for (CommandInfo c : commands) {
            lines.add("  " + c.syntax());
        }
        return String.join(System.lineSeparator(), lines);
    }
}
