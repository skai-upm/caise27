package es.upm.parque.presentation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The help commands registered during initialization, each answered by its own {@link HelpGenerator}. Command
 * processing only asks whether a command is a help command and delegates it here.
 */
public class HelpCommands {

    private final Map<String, HelpGenerator> generators = new LinkedHashMap<>();

    /** Registers a help command answered by the given generator. */
    public HelpCommands register(String command, HelpGenerator generator) {
        generators.put(command, generator);
        return this;
    }

    public boolean handles(String command) {
        return generators.containsKey(command);
    }

    /** Generates the help for a registered help command, which takes no fields. */
    public String respond(String[] elements) throws InvalidCommandException {
        CommandProcessor.expect(elements, 1, elements[0] + " takes no fields");
        return generators.get(elements[0]).generate();
    }
}
