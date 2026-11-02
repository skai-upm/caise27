package com.amusementpark.presentation;

import java.util.function.Function;

/**
 * A registered command: its name, accepted parameter counts, help texts and action.
 * The action receives the split command parts and returns the text to print.
 */
public record CommandDefinition(String name, int minParameters, int maxParameters, String syntax,
                                String description, Function<String[], String> action) {

    public CommandDefinition(String name, int parameters, String syntax, String description,
                             Function<String[], String> action) {
        this(name, parameters, parameters, syntax, description, action);
    }

    public boolean accepts(int parameters) {
        return parameters >= minParameters && parameters <= maxParameters;
    }

    public HelpEntry helpEntry() {
        return new HelpEntry(syntax, description);
    }
}
