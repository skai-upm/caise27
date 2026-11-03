package es.upm.etsisi.amusementpark.presentation.help;

/** Help information of one supported command: its syntax and a brief description of its behaviour. */
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
