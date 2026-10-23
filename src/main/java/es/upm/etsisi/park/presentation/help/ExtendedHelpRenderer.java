package es.upm.etsisi.park.presentation.help;

import java.util.List;

import es.upm.etsisi.park.presentation.CommandDefinition;

/** HELP EXTENDED: every syntax followed by the purpose of the command and its notes. */
public class ExtendedHelpRenderer implements HelpRenderer {

    @Override
    public String render(List<CommandDefinition> commands) {
        StringBuilder sb = new StringBuilder("Available commands (extended):\n");
        for (CommandDefinition command : commands) {
            sb.append('\n');
            for (String syntax : command.syntaxes()) {
                sb.append("  ").append(syntax).append('\n');
            }
            sb.append("      ").append(command.description()).append('\n');
            for (String note : command.notes()) {
                sb.append("      ").append(note).append('\n');
            }
        }
        return sb.toString();
    }
}
