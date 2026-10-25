package es.upm.parque.presentation;

/**
 * A registered command: its syntax and a brief description of its behavior.
 */
public class CommandInfo {

    private final String syntax;
    private final String description;

    public CommandInfo(String syntax, String description) {
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
