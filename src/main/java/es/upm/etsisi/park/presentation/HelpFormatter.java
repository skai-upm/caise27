package es.upm.etsisi.park.presentation;

import java.util.List;

/**
 * Strategy that turns the command registry into help text.
 */
public interface HelpFormatter {

    String format(List<CommandDefinition> commands);
}
