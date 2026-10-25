package es.upm.parque.presentation.command;

import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.CommandRegistry;
import es.upm.parque.presentation.help.HelpGenerator;

/**
 * HELP, HELP EXTENDED and EXIT. Register last so the help lists every command.
 */
public final class HelpCommands {

    private HelpCommands() {
    }

    public static void register(CommandRegistry registry, HelpGenerator concise, HelpGenerator extended) {
        registry.register("HELP", "Lists the syntax of every command.", p -> concise.generate(registry));
        registry.register("HELP EXTENDED", "Lists the syntax of every command with a short description.",
                p -> extended.generate(registry));
        registry.register(CommandProcessor.EXIT, "Ends the application; nothing after it is executed.",
                p -> "OK EXIT");
    }
}
