package es.upm.parque.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * The commands known by the application, in registration order. A command name may be registered
 * several times with different parameter counts (optional parameters).
 */
public class CommandRegistry {

    private final List<CommandDefinition> definitions = new ArrayList<>();

    /**
     * Registers a command from its syntax ({@code NAME;Param1;Param2}).
     */
    public void register(String syntax, String description, Function<String[], String> action) {
        String[] parts = syntax.split(";");
        definitions.add(new CommandDefinition(parts[0], syntax, description, parts.length - 1, action));
    }

    public List<CommandDefinition> definitions() {
        return Collections.unmodifiableList(definitions);
    }

    public List<CommandDefinition> named(String name) {
        return definitions.stream().filter(definition -> definition.name().equals(name)).toList();
    }
}
