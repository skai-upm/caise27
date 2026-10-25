package com.amusementpark.presentation.help;

import java.util.List;

/**
 * Strategy that turns the registered commands into a help text.
 */
public interface HelpFormat {

    String format(List<CommandInfo> commands);
}
