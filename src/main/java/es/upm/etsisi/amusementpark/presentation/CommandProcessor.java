package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.BusinessException;

/**
 * Interprets single {@code ;}-separated command lines using the registered commands. Business
 * and syntax exceptions are reported as errors so processing always continues.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private final CommandRegistry registry;

    public CommandProcessor(CommandRegistry registry) {
        this.registry = registry;
    }

    public CommandResult process(String line) {
        String[] parts = line.trim().split(SEPARATOR, -1);
        CommandSpec spec = registry.find(parts[0]).orElse(null);
        if (spec == null) {
            return CommandResult.error("unknown command '" + parts[0] + "' (type HELP)");
        }
        if (!spec.arities().contains(parts.length)) {
            return CommandResult.error("wrong number of arguments, usage: " + String.join(" | ", spec.syntaxes()));
        }
        try {
            return spec.handler().handle(parts);
        } catch (BusinessException | CommandSyntaxException e) {
            return CommandResult.error(e.getMessage());
        }
    }
}
