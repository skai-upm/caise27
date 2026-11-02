package es.upm.etsisi.amusementpark.presentation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The commands the application recognises, in registration order.
 */
public class CommandRegistry {

    private final Map<String, CommandSpec> commands = new LinkedHashMap<>();

    public void register(CommandSpec spec) {
        if (commands.putIfAbsent(spec.name(), spec) != null) {
            throw new IllegalArgumentException("command already registered: " + spec.name());
        }
    }

    public Optional<CommandSpec> find(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    public List<CommandSpec> specs() {
        return new ArrayList<>(commands.values());
    }
}
