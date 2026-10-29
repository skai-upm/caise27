package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Strategy that turns the command catalogue into help text.
 */
public interface HelpFormat {

    String format(List<CommandInfo> commands);
}
