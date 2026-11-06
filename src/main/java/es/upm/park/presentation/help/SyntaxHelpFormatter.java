package es.upm.park.presentation.help;

import java.util.List;

/** Basic help: one syntax line per command. */
public class SyntaxHelpFormatter implements HelpFormatter {

    @Override
    public String format(List<CommandDescription> commands) {
        StringBuilder out = new StringBuilder("Available commands:");
        commands.forEach(command -> out.append(System.lineSeparator()).append(command.syntax()));
        return out.toString();
    }
}
