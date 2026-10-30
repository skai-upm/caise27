package es.upm.etsisi.amusementpark.presentation.command;

/**
 * A command variant: its name, exact syntax, purpose and behaviour.
 */
public record CommandDescriptor(String name, String syntax, String description, CommandAction action) {

    public int fieldCount() {
        return syntax.split(";", -1).length;
    }
}
