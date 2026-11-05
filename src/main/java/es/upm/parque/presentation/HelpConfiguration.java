package es.upm.parque.presentation;

/**
 * Configures the help variants available in the application: HELP (syntax) and HELP EXTENDED (syntax and purpose).
 */
public final class HelpConfiguration {

    static final String HELP = "HELP";
    static final String HELP_EXTENDED = "HELP EXTENDED";

    private HelpConfiguration() {
    }

    public static HelpCommands standard() {
        return new HelpCommands()
                .register(HELP, new HelpGenerator(CommandCatalog.COMMANDS, new SyntaxHelpFormatter()))
                .register(HELP_EXTENDED, new HelpGenerator(CommandCatalog.COMMANDS, new ExtendedHelpFormatter()));
    }
}
