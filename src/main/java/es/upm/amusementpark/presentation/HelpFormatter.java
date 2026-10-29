package es.upm.amusementpark.presentation;

import java.util.List;

/** Behavior used by {@link HelpGenerator} to turn the command catalog into help text. */
public interface HelpFormatter {

    String format(List<CommandInfo> commands);
}
