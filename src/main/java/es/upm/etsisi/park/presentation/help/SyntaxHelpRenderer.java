package es.upm.etsisi.park.presentation.help;

import java.util.List;

import es.upm.etsisi.park.presentation.CommandDefinition;

/** HELP: every accepted syntax, plus notes such as allowed values. */
public class SyntaxHelpRenderer implements HelpRenderer {

    @Override
    public String render(List<CommandDefinition> commands) {
        StringBuilder sb = new StringBuilder("Available commands:\n");
        for (CommandDefinition command : commands) {
            for (String syntax : command.syntaxes()) {
                sb.append("  ").append(syntax).append('\n');
            }
            for (String note : command.notes()) {
                sb.append("      ").append(note).append('\n');
            }
        }
        return sb.toString();
    }
}
