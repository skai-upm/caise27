package es.upm.parque.presentation;

import es.upm.parque.business.ParkException;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interprets one text command and executes it. Shared by interactive and file modes.
 */
public class CommandProcessor {

    /**
     * Outcome of processing a command: the text to show and whether processing must stop.
     */
    public record Result(String output, boolean exit) {

        static Result error(String message) {
            return new Result("ERROR: " + message, false);
        }
    }

    public static final String EXIT = "EXIT";

    private final CommandRegistry registry;

    public CommandProcessor(CommandRegistry registry) {
        this.registry = registry;
    }

    public Result process(String line) {
        String[] parts = line.trim().split(";", -1);
        String name = parts[0].trim();
        List<CommandDefinition> candidates = registry.named(name);
        if (candidates.isEmpty()) {
            return Result.error("unknown command '" + name + "'. Type HELP to list the commands");
        }
        CommandDefinition command = candidates.stream()
                .filter(candidate -> candidate.parameters() == parts.length - 1)
                .findFirst().orElse(null);
        if (command == null) {
            return Result.error("wrong number of parameters. Usage: "
                    + candidates.stream().map(CommandDefinition::syntax).collect(Collectors.joining(" | ")));
        }
        try {
            return new Result(command.action().apply(parts), EXIT.equals(name));
        } catch (ParkException e) {
            return Result.error(e.getMessage());
        } catch (UncheckedIOException e) {
            return Result.error("storage failure: " + e.getMessage());
        }
    }
}
