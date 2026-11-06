package es.upm.park.presentation.help;

import java.util.List;

/** Behavior that turns command descriptions into help text (Strategy). */
@FunctionalInterface
public interface HelpFormatter {

    String format(List<CommandDescription> commands);
}
