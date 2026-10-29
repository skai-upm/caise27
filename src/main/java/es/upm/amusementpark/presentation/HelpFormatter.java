package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Strategy that turns the command catalog into help text. New help formats are added by
 * implementing this interface and injecting it into a {@link HelpGenerator}.
 */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
