package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Strategy deciding how help text is built from the command catalogue.
 */
public interface HelpFormat {

    String format(List<CommandCatalog.CommandInfo> commands);
}
