package es.upm.etsisi.amusementpark.presentation;

import java.io.PrintStream;
import java.util.List;

/**
 * HELP and HELP EXTENDED: display the help produced by the injected generator.
 */
public class HelpCommand implements Command {

    /** Syntax lines of every command, as listed by HELP. */
    public static final List<String> SYNTAX = CommandCatalog.ENTRIES.stream().map(CommandHelp::syntax).toList();

    private final HelpGenerator generator;

    public HelpCommand(HelpGenerator generator) {
        this.generator = generator;
    }

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        out.println(generator.generate());
        return false;
    }
}
