package es.upm.amusementpark.presentation;

import java.util.Objects;

/** Syntax and purpose of one command, used to generate help text. */
public record CommandInfo(String syntax, String description) {

    public CommandInfo {
        Objects.requireNonNull(syntax);
        Objects.requireNonNull(description);
    }
}
