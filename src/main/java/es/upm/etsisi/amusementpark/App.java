package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard input;
 * with one argument they are read from the given file.
 */
public class App {

    public static void main(String[] args) {
        System.exit(run(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, System.err, UserRepository.DEFAULT_FILE));
    }

    /**
     * Runs the application and returns the process exit status.
     */
    static int run(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, Path usersFile) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserService(new UserRepository(usersFile)));
        } catch (RuntimeException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        if (args.length == 0) {
            return execute(processor, stdin, out, err);
        }
        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            err.println("ERROR: command file not found or not readable: " + commandFile);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            return execute(processor, reader, out, err);
        } catch (IOException e) {
            err.println("ERROR: cannot read command file: " + e.getMessage());
            return 1;
        }
    }

    private static int execute(CommandProcessor processor, BufferedReader input, PrintStream out, PrintStream err) {
        try {
            processor.run(input, out);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read commands: " + e.getMessage());
            return 1;
        }
    }
}
