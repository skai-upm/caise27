package es.upm.etsisi.amusementpark.presentation;

/**
 * One command form: its syntax (elements separated by ';') and what it does.
 */
public record CommandSpec(String syntax, String description) {

    /**
     * The text before the first ';', which identifies the command.
     */
    public String keyword() {
        int separator = syntax.indexOf(';');
        return separator < 0 ? syntax : syntax.substring(0, separator);
    }

    /**
     * Number of ';'-separated elements, keyword included.
     */
    public int arity() {
        return syntax.split(";", -1).length;
    }
}
