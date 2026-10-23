package es.upm.etsisi.park.presentation.help;

import java.util.List;

import es.upm.etsisi.park.presentation.CommandDefinition;

/** One way of presenting the available commands as help text. */
@FunctionalInterface
public interface HelpRenderer {

    String render(List<CommandDefinition> commands);
}
