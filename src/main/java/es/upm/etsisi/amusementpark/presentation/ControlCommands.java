package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;

/**
 * HELP, HELP EXTENDED and EXIT.
 */
public final class ControlCommands {

    private ControlCommands() {
    }

    public static void register(CommandRegistry registry, HelpGenerator help, HelpGenerator extendedHelp) {
        registry.register(CommandSpec.of("HELP", "Lists the syntax of every command.",
                p -> CommandResult.message(help.generate())));
        registry.register(CommandSpec.of("HELP EXTENDED", "Lists every command with a short explanation.",
                p -> CommandResult.message(extendedHelp.generate())));
        registry.register(CommandSpec.of("EXIT", "Ends the execution; later commands are not processed.",
                p -> new CommandResult("BYE", true)));
    }
}
