package es.upm.etsisi.amusementpark.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads commands one per line from any line source (standard input or a file)
 * and executes them until EXIT is processed or the input ends.
 */
public class CommandProcessor {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CommandProcessor() {
        register("SYSTEM DATE", new SystemDateCommand());
        register("SYSTEM PRICE", new SystemPriceCommand());
        register("GROUP CREATE", new GroupCreateCommand());
        register("GROUP ADD", new GroupAddCommand());
        register("GROUP REMOVE", new GroupRemoveCommand());
        register("HELP", new HelpCommand());
        register("EXIT", new ExitCommand());
    }

    public final void register(String keyword, Command command) {
        commands.put(keyword, command);
    }

    /**
     * Processes the lines of {@code in}. No line after an EXIT command is read.
     */
    public void run(BufferedReader in, PrintStream out) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (process(line, out)) {
                return;
            }
        }
    }

    /**
     * Processes a single command line.
     *
     * @return {@code true} if processing must stop
     */
    public boolean process(String line, PrintStream out) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String[] fields = trimmed.split(";", -1);
        Command command = commands.get(fields[0].trim());
        if (command == null) {
            out.println("ERROR: Unknown command: " + trimmed);
            return false;
        }
        try {
            return command.execute(Arrays.copyOfRange(fields, 1, fields.length), out);
        } catch (RuntimeException e) {
            out.println("ERROR: Could not execute command: " + trimmed
                    + (e.getMessage() == null ? "" : " (" + e.getMessage() + ")"));
            return false;
        }
    }
}
