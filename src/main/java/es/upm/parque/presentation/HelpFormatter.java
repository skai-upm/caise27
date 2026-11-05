package es.upm.parque.presentation;

import java.util.List;

/**
 * Strategy that builds a help text from the command catalog; new help variants implement it.
 */
@FunctionalInterface
public interface HelpFormatter {

    String format(List<CommandDescriptor> commands);
}
