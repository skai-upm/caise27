package es.upm.etsisi.amusementpark.presentation.help;

/**
 * Help information about one supported command.
 *
 * @param operation   the operation name as dispatched, e.g. {@code USER CREATE}
 * @param syntax      the command syntax shown by HELP
 * @param description a brief explanation shown by HELP EXTENDED
 */
public record CommandInfo(String operation, String syntax, String description) {
}
