package com.amusementpark.presentation;

import java.util.List;

/**
 * Presentation format of the help text; new formats implement this interface.
 */
public interface HelpFormatter {

    String format(List<HelpEntry> entries);
}
