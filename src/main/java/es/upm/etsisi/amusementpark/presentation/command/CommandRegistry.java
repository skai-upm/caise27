package es.upm.etsisi.amusementpark.presentation.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Ordered table of command variants. A command name may have several variants with different
 * numbers of fields.
 */
public class CommandRegistry {

    private final List<CommandDescriptor> commands = new ArrayList<>();

    /**
     * Registers a variant; its name is the first ';'-separated element of the syntax.
     */
    public void register(String syntax, String description, CommandAction action) {
        commands.add(new CommandDescriptor(syntax.split(";", -1)[0], syntax, description, action));
    }

    public List<CommandDescriptor> all() {
        return List.copyOf(commands);
    }

    public List<CommandDescriptor> variants(String name) {
        return commands.stream().filter(c -> c.name().equals(name)).toList();
    }
}
