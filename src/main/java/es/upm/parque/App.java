package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Amusement park application entry point.
 * Without arguments it reads commands from standard input; with one argument it reads
 * them from the given file. Both modes share the same command processing.
 */
public class App {

    public static void main(String[] args) {
        UserManager userManager;
        try {
            userManager = new UserManager(new UserRepository());
        } catch (RuntimeException e) {
            System.out.println("ERROR: cannot load stored users: " + e.getMessage());
            return;
        }
        CommandProcessor processor = new CommandProcessor(userManager);

        if (args.length == 0) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            run(stdin, processor, System.out, false);
            return;
        }

        Path file = Path.of(args[0]);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            run(reader, processor, System.out, true);
        } catch (IOException e) {
            System.out.println("ERROR: cannot read command file '" + args[0] + "': " + describe(e));
        }
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     * Read errors are reported and stop processing.
     *
     * @param echo whether each command is printed before its result (useful for file input)
     */
    public static void run(BufferedReader input, CommandProcessor processor, PrintStream out, boolean echo) {
        try {
            String line;
            while ((line = input.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (echo) {
                    out.println("> " + line);
                }
                CommandProcessor.Result result = processor.process(line);
                out.println(result.output());
                if (result.exit()) {
                    return;
                }
            }
        } catch (IOException e) {
            out.println("ERROR: cannot read commands: " + describe(e));
        }
    }

    private static String describe(IOException e) {
        if (e instanceof java.nio.file.NoSuchFileException) {
            return "file does not exist";
        }
        if (e instanceof java.nio.file.AccessDeniedException) {
            return "permission denied";
        }
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
