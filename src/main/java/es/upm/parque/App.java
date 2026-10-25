package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Entry point of the Amusement Park application. Without arguments it reads commands from standard input;
 * with an argument it reads them from the file at that path.
 */
public class App {

    static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) {
        int code = run(args, System.in, System.out, USERS_FILE);
        if (code != 0) {
            System.exit(code);
        }
    }

    /**
     * Runs the application and returns the process exit code.
     */
    static int run(String[] args, InputStream stdin, PrintStream out, Path usersFile) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserRepository(usersFile)));
        } catch (IOException e) {
            out.println("ERROR Cannot load users from " + usersFile + ": " + e.getMessage());
            return 1;
        }
        if (args.length == 0) {
            out.println("Amusement Park - type HELP to see the available commands");
            return processAll(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), processor,
                    out, "standard input");
        }
        String path = args[0];
        try (BufferedReader reader = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            return processAll(reader, processor, out, path);
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR Cannot read command file: " + path);
            return 1;
        }
    }

    private static int processAll(BufferedReader reader, CommandProcessor processor, PrintStream out, String source) {
        try {
            String line;
            while (!processor.isFinished() && (line = reader.readLine()) != null) {
                String result = processor.process(line);
                if (result != null) {
                    out.println(result);
                }
            }
            return 0;
        } catch (IOException e) {
            out.println("ERROR Cannot read commands from " + source);
            return 1;
        }
    }
}
