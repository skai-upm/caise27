package es.upm.parque.presentation.help;

/**
 * A registered command: its syntax and a brief explanation of what it does.
 */
public class CommandDescriptor {
    private final String syntax;
    private final String description;

    public CommandDescriptor(String syntax, String description) {
        this.syntax = syntax;
        this.description = description;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getDescription() {
        return description;
    }
}
