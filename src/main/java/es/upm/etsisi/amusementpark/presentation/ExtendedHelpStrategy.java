package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Help listing each command's syntax followed by a brief explanation of its purpose.
 */
public class ExtendedHelpStrategy implements HelpStrategy {

    @Override
    public String render(List<CommandHelp> entries) {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder("Available commands (extended):");
        for (CommandHelp entry : entries) {
            text.append(nl).append(entry.syntax()).append(nl).append("    ").append(entry.description());
        }
        return text.toString();
    }
}
