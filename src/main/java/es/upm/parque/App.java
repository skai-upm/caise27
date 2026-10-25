package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Amusement Park application entry point.
 * Without arguments commands are read from standard input; with one argument they are read from that file.
 */
public class App {

    static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) {
        run(args, USERS_FILE, System.out);
    }

    static void run(String[] args, Path usersFile, PrintStream out) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserRepository(usersFile)));
        } catch (IOException e) {
            out.println("ERROR could not load stored users: " + e.getMessage());
            return;
        }
        if (args.length == 0) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            processLines(stdin, processor, out);
        } else {
            Path commandFile = Path.of(args[0]);
            if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
                out.println("ERROR cannot open command file: " + args[0]);
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
                processLines(reader, processor, out);
            } catch (IOException e) {
                out.println("ERROR cannot read command file: " + args[0]);
            }
        }
    }

    private static void processLines(BufferedReader reader, CommandProcessor processor, PrintStream out) {
        try {
            String line;
            while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                out.println(processor.process(line));
            }
        } catch (IOException e) {
            out.println("ERROR reading commands: " + e.getMessage());
        }
    }
}
