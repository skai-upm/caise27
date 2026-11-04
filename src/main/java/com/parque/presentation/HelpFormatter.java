package com.parque.presentation;

import java.util.List;

/**
 * Strategy that turns the registered commands into help text. New help formats implement this interface.
 */
public interface HelpFormatter {
    String format(List<CommandInfo> commands);
}
