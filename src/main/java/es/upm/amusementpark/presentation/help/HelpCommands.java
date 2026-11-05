package es.upm.amusementpark.presentation.help;

import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.PrintStream;

/**
 * {@code HELP} and {@code HELP EXTENDED} commands, backed by injected generators.
 */
public final class HelpCommands {

    private HelpCommands() {
    }

    public static void register(CommandProcessor processor, HelpGenerator plain, HelpGenerator extended,
            PrintStream out) {
        processor.register("HELP", line -> {
            out.println(plain.generate());
            return true;
        });
        processor.register("HELP EXTENDED", line -> {
            out.println(extended.generate());
            return true;
        });
    }

    /** Registers help with the standard catalog and the syntax / extended formatters. */
    public static void registerDefault(CommandProcessor processor, PrintStream out) {
        CommandCatalog catalog = CommandCatalog.standard();
        register(processor, new HelpGenerator(new SyntaxHelpFormatter(), catalog),
                new HelpGenerator(new ExtendedHelpFormatter(), catalog), out);
    }
}
