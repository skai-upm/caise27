package es.upm.parque.presentation;

import es.upm.parque.presentation.help.HelpGenerator;

/**
 * HELP, HELP EXTENDED and EXIT.
 */
public class HelpCommands implements CommandSet {

    private final HelpGenerator syntaxHelp;
    private final HelpGenerator extendedHelp;

    public HelpCommands(HelpGenerator syntaxHelp, HelpGenerator extendedHelp) {
        this.syntaxHelp = syntaxHelp;
        this.extendedHelp = extendedHelp;
    }

    @Override
    public void registerInto(CommandRegistry registry) {
        registry.register("HELP", "", "Lists the available commands and their syntax.",
                p -> syntaxHelp.generate(registry.infos()));
        registry.register("HELP EXTENDED", "", "Lists the available commands with a description of each.",
                p -> extendedHelp.generate(registry.infos()));
        registry.register(CommandProcessor.EXIT, "", "Ends the application (in a file, later lines are ignored).",
                p -> "OK EXIT");
    }
}
