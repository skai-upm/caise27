package es.upm.parque.presentation;

import java.util.Objects;

/**
 * A recognized command: its syntax and a brief explanation of its purpose.
 */
public record CommandDescriptor(String syntax, String purpose) {

    public CommandDescriptor {
        Objects.requireNonNull(syntax);
        Objects.requireNonNull(purpose);
    }
}
