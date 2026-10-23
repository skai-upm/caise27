package es.upm.etsisi.park.presentation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Commands by keyword, in registration order (the order used by HELP). */
public class CommandRegistry {

    private final Map<String, CommandDefinition> commands = new LinkedHashMap<>();

    public void register(CommandDefinition definition) {
        if (commands.putIfAbsent(definition.keyword(), definition) != null) {
            throw new IllegalArgumentException("Command already registered: " + definition.keyword());
        }
    }

    public void registerAll(CommandGroup... groups) {
        for (CommandGroup group : groups) {
            group.register(this);
        }
    }

    public CommandDefinition find(String keyword) {
        return commands.get(keyword);
    }

    public List<CommandDefinition> definitions() {
        return new ArrayList<>(commands.values());
    }
}
