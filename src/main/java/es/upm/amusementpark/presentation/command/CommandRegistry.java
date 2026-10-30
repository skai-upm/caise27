package es.upm.amusementpark.presentation.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ordered set of the commands supported by the application. Used both for dispatching and for help.
 */
public class CommandRegistry {

    private final Map<String, CommandDefinition> commands = new LinkedHashMap<>();

    public CommandRegistry register(CommandDefinition definition) {
        commands.put(definition.name(), definition);
        return this;
    }

    public CommandDefinition find(String name) {
        return commands.get(name);
    }

    public List<CommandDefinition> all() {
        return new ArrayList<>(commands.values());
    }
}
