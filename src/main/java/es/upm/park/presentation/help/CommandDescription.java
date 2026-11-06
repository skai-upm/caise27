package es.upm.park.presentation.help;

/** A command's syntax and a brief explanation of its purpose. */
public record CommandDescription(String syntax, String purpose) {

    /** The command keyword: the syntax up to the first ';'. */
    public String keyword() {
        int separator = syntax.indexOf(';');
        return separator < 0 ? syntax : syntax.substring(0, separator);
    }
}
