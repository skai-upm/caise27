package es.upm.parque.presentation;

import java.util.List;

/**
 * Help text with the syntax of each command (the HELP command).
 */
public class SyntaxHelpFormatter implements HelpFormatter {

    private static final String NL = System.lineSeparator();

    @Override
    public String format(List<CommandDescriptor> commands) {
        StringBuilder text = new StringBuilder("Available commands:");
        commands.forEach(c -> text.append(NL).append("  ").append(c.syntax()));
        return text.append(NL).append("  (USER UPDATE <Field>: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)").toString();
    }
}
