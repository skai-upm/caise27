package com.amusementpark.presentation.help;

import java.util.List;
import java.util.Map;

/**
 * Produces help texts from the registered commands using the injected formats, keyed by the
 * help variant ("" for plain HELP, "EXTENDED" for HELP EXTENDED, ...).
 */
public class HelpGenerator {
    private final List<CommandInfo> commands;
    private final Map<String, HelpFormat> formats;

    public HelpGenerator(List<CommandInfo> commands, Map<String, HelpFormat> formats) {
        this.commands = List.copyOf(commands);
        this.formats = Map.copyOf(formats);
    }

    public boolean supports(String variant) {
        return formats.containsKey(variant);
    }

    public String generate(String variant) {
        HelpFormat format = formats.get(variant);
        return format == null ? "ERROR: unknown help variant " + variant : format.format(commands);
    }
}
