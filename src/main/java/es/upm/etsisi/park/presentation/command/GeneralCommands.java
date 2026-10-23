package es.upm.etsisi.park.presentation.command;

import es.upm.etsisi.park.presentation.Args;
import es.upm.etsisi.park.presentation.CommandDefinition;
import es.upm.etsisi.park.presentation.CommandGroup;
import es.upm.etsisi.park.presentation.CommandRegistry;
import es.upm.etsisi.park.presentation.help.HelpGenerator;

/** HELP, HELP EXTENDED and EXIT. */
public class GeneralCommands implements CommandGroup {

    private final HelpGenerator help;

    public GeneralCommands(HelpGenerator help) {
        this.help = help;
    }

    @Override
    public void register(CommandRegistry registry) {
        registry.register(CommandDefinition.of("HELP", "Lists the syntax of every command.",
                (args, out) -> {
                    Args.require(args, "HELP", 0);
                    out.print(help.render(HelpGenerator.BASIC));
                }, "HELP"));
        registry.register(CommandDefinition.of("HELP EXTENDED", "Lists every command with a description of its purpose.",
                (args, out) -> {
                    Args.require(args, "HELP EXTENDED", 0);
                    out.print(help.render(HelpGenerator.EXTENDED));
                }, "HELP EXTENDED"));
        registry.register(CommandDefinition.of("EXIT", "Ends the application; no further command is processed.",
                (args, out) -> {
                    Args.require(args, "EXIT", 0);
                    out.println("BYE");
                }, "EXIT").terminating());
    }
}
