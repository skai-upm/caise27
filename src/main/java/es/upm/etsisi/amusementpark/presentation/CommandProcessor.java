package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.persistence.PersistenceException;
import es.upm.etsisi.amusementpark.presentation.command.CommandDescriptor;
import es.upm.etsisi.amusementpark.presentation.command.CommandRegistry;
import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;
import es.upm.etsisi.amusementpark.presentation.help.HelpMode;

import java.io.PrintStream;
import java.util.List;

/**
 * Interprets a single command line, executes it and prints the outcome. Exceptions raised by
 * commands are reported here and never stop processing.
 */
public class CommandProcessor {

    static final String EXIT = "EXIT";

    private final CommandRegistry registry;
    private final PrintStream out;

    public CommandProcessor(CommandRegistry registry, HelpGenerator help, PrintStream out) {
        this.registry = registry;
        this.out = out;
        registry.register("HELP", "Lists the syntax of every command.",
                p -> help.generate(HelpMode.BASIC, registry.all()));
        registry.register("HELP EXTENDED", "Lists every command with a description of its purpose.",
                p -> help.generate(HelpMode.EXTENDED, registry.all()));
        registry.register(EXIT, "Ends the application; later commands are not processed.", p -> "BYE");
    }

    /**
     * Processes one command line.
     *
     * @return false if the application must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.trim().split(";", -1);
        String name = parts[0].trim();
        try {
            out.println(select(name, parts.length).action().execute(parts));
            return !EXIT.equals(name);
        } catch (ParkException | CommandSyntaxException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (PersistenceException e) {
            out.println("ERROR: persistence failure: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR: Command could not be processed: " + e.getMessage());
        }
        return true;
    }

    private CommandDescriptor select(String name, int fieldCount) {
        List<CommandDescriptor> variants = registry.variants(name);
        if (variants.isEmpty()) {
            throw new CommandSyntaxException("Unknown command: " + name + " (type HELP)");
        }
        return variants.stream()
                .filter(c -> c.fieldCount() == fieldCount)
                .findFirst()
                .orElseThrow(() -> new CommandSyntaxException("Wrong number of arguments. Usage: "
                        + String.join(" | ", variants.stream().map(CommandDescriptor::syntax).toList())));
    }
}
