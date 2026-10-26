package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Entry point. Without arguments commands are read interactively from standard input; with an
 * argument, the first argument is a file whose lines are executed as commands. Both modes share
 * the same processing.
 */
public class App {

    static final Path USERS_FILE = Path.of("data", "users.txt");
    static final Path GROUPS_FILE = Path.of("data", "groups.txt");

    private final CommandProcessor processor;

    public App(CommandProcessor processor) {
        this.processor = processor;
    }

    public static void main(String[] args) {
        MainController mainController = new MainController(Clock.systemDefaultZone());
        UserManager userManager = new UserManager(new UserRepository(USERS_FILE));
        GroupManager groupManager = new GroupManager(new GroupRepository(GROUPS_FILE), userManager);
        new App(new CommandProcessor(userManager, groupManager, mainController)).start(args, System.in, System.out);
    }

    /**
     * Selects the execution mode from the arguments and runs it.
     */
    public void start(String[] args, InputStream stdin, PrintStream out) {
        if (args.length == 0) {
            run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            return;
        }
        Path file = Path.of(args[0]);
        if (Files.notExists(file)) {
            out.println("ERROR Input file not found: " + file);
            return;
        }
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            run(in, out);
        } catch (IOException e) {
            out.println("ERROR Cannot read input file: " + file);
        }
    }

    /**
     * Processes every non-blank line until the input ends or an EXIT command is processed.
     */
    public void run(BufferedReader in, PrintStream out) {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                out.println(processor.process(line));
                if (processor.isExitRequested()) {
                    return;
                }
            }
        } catch (IOException e) {
            out.println("ERROR Cannot read input: " + e.getMessage());
        }
    }
}
