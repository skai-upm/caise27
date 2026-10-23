package es.upm.etsisi.park.presentation;

import java.util.List;

/**
 * A command known to the application: its keyword, accepted syntaxes, a
 * short description of its purpose, optional notes (e.g. allowed values),
 * whether it ends the session, and the handler that runs it.
 */
public record CommandDefinition(String keyword, List<String> syntaxes, String description, List<String> notes,
                                boolean terminates, CommandHandler handler) {

    public CommandDefinition {
        syntaxes = List.copyOf(syntaxes);
        notes = List.copyOf(notes);
    }

    public static CommandDefinition of(String keyword, String description, CommandHandler handler,
                                       String... syntaxes) {
        return new CommandDefinition(keyword, List.of(syntaxes), description, List.of(), false, handler);
    }

    public CommandDefinition withNotes(String... extraNotes) {
        return new CommandDefinition(keyword, syntaxes, description, List.of(extraNotes), terminates, handler);
    }

    public CommandDefinition terminating() {
        return new CommandDefinition(keyword, syntaxes, description, notes, true, handler);
    }
}
