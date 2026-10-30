package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point: starts the application and handles terminal interaction.
 * Without arguments commands are read from standard input; with an argument, from that file.
 */
public class App {

    static final String USERS_FILE = "users.txt";

    public static void main(String[] args) {
        run(args, Path.of(USERS_FILE), System.out);
    }

    static void run(String[] args, Path usersFile, PrintStream out) {
        UserService userService;
        try {
            userService = new UserService(new UserRepository(usersFile));
        } catch (IOException e) {
            out.println("ERROR: could not load users from " + usersFile + ": " + e.getMessage());
            return;
        }
        CommandProcessor processor = new CommandProcessor(userService, out);
        if (args.length == 0) {
            runCommands(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), processor, out);
        } else {
            Path commandFile = Path.of(args[0]);
            try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
                runCommands(reader, processor, out);
            } catch (IOException e) {
                out.println("ERROR: cannot open command file " + commandFile);
            }
        }
    }

    /** Shared loop for every input mode: one command per non-blank line until EXIT or end of input. */
    static void runCommands(BufferedReader reader, CommandProcessor processor, PrintStream out) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (!processor.process(line)) {
                    return;
                }
            }
        } catch (IOException e) {
            out.println("ERROR: error reading commands: " + e.getMessage());
        }
    }
}
