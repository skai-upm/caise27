package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

import es.upm.etsisi.amusementpark.presentation.CommandSpec;

/**
 * A way of rendering the command catalog as help text.
 */
public interface HelpStrategy {
    String render(List<CommandSpec> commands);
}
