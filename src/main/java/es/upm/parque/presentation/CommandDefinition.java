package es.upm.parque.presentation;

import java.util.function.Function;

/**
 * A registered command: name (e.g. {@code USER CREATE}), syntax and description for help,
 * number of parameters and the action producing the text to show.
 */
public record CommandDefinition(String name, String syntax, String description, int parameters,
                                Function<String[], String> action) {
}
