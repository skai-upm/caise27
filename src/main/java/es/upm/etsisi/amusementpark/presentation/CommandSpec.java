package es.upm.etsisi.amusementpark.presentation;

import java.util.List;
import java.util.Set;

/**
 * A registered command: its name, the syntax variants shown by HELP, a description shown by
 * HELP EXTENDED, the accepted numbers of {@code ;}-separated parts and its handler.
 */
public record CommandSpec(String name, List<String> syntaxes, String description, Set<Integer> arities,
                          CommandHandler handler) {

    public CommandSpec {
        syntaxes = List.copyOf(syntaxes);
        arities = Set.copyOf(arities);
    }

    public static CommandSpec of(String syntax, String description, CommandHandler handler) {
        String name = syntax.contains(";") ? syntax.substring(0, syntax.indexOf(';')) : syntax;
        return new CommandSpec(name, List.of(syntax), description, Set.of(syntax.split(";", -1).length), handler);
    }
}
