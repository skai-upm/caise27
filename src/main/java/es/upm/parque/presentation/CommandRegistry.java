package es.upm.parque.presentation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.presentation.help.CommandInfo;

/**
 * The available commands, in registration order. A command may be registered with a fixed first
 * parameter as part of its key (e.g. {@code INVOICE CREATE;USER}).
 */
public class CommandRegistry {

    private static final String SEPARATOR = ";";

    private final Map<String, Command> commands = new LinkedHashMap<>();

    /**
     * Registers a command. Parameters after a {@code [} in {@code params} are optional.
     */
    public void register(String key, String params, String description, CommandHandler handler) {
        int bracket = params.indexOf('[');
        String required = bracket < 0 ? params : params.substring(0, bracket);
        String optional = bracket < 0 ? "" : params.substring(bracket);
        int min = count(required);
        int max = min + (int) optional.chars().filter(c -> c == ';').count();
        commands.put(key, new Command(key, params, description, min, max, handler));
    }

    /**
     * Finds the command for a name, preferring a registration that includes the first parameter.
     *
     * @return the command and whether the first parameter was consumed as part of the key, or null
     */
    public Match find(String name, String[] params) {
        if (params.length > 0) {
            Command withKeyword = commands.get(name + SEPARATOR + params[0].toUpperCase());
            if (withKeyword != null) {
                return new Match(withKeyword, true);
            }
        }
        Command command = commands.get(name);
        return command == null ? null : new Match(command, false);
    }

    public List<CommandInfo> infos() {
        return commands.values().stream().map(Command::info).toList();
    }

    private static int count(String required) {
        String trimmed = required.endsWith(SEPARATOR) ? required.substring(0, required.length() - 1) : required;
        return trimmed.isEmpty() ? 0 : trimmed.split(SEPARATOR).length;
    }

    /**
     * Result of a lookup.
     *
     * @param command        the command found
     * @param keywordInName  whether the first parameter belongs to the command key
     */
    public record Match(Command command, boolean keywordInName) {
    }
}
