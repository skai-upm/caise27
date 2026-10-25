package es.upm.parque.presentation.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The commands registered at startup, in registration order.
 */
public class CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    public void register(Command command) {
        commands.put(command.getKeyword(), command);
    }

    /**
     * Returns the command for the keyword, or {@code null} if none is registered.
     */
    public Command find(String keyword) {
        return commands.get(keyword);
    }

    public List<Command> getCommands() {
        return new ArrayList<>(commands.values());
    }
}
