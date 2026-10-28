package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Help listing only the syntax of each command.
 */
public class SyntaxHelpStrategy implements HelpStrategy {

    @Override
    public String render(List<CommandHelp> entries) {
        StringBuilder text = new StringBuilder("Available commands:");
        entries.forEach(entry -> text.append(System.lineSeparator()).append(entry.syntax()));
        return text.toString();
    }
}
