package es.upm.amusementpark.presentation.help;

/**
 * Description of one command for help output.
 *
 * @param syntax command syntax, for example {@code GROUP DELETE;<Identifier>}
 * @param purpose brief explanation of what the command does
 */
public record CommandDescription(String syntax, String purpose) {
}
