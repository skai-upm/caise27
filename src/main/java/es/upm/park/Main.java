package es.upm.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import es.upm.park.command.CommandProcessor;
import es.upm.park.persistence.UserFileStore;
import es.upm.park.repository.UserRepository;
import es.upm.park.service.UserService;

/**
 * Entry point of the Amusement Park application: wires the components and
 * feeds commands read from standard input to the command processor.
 */
public final class Main {

    static final String BANNER = "Amusement Park application started";
    static final String USERS_FILE_PROPERTY = "park.users.file";
    static final String DEFAULT_USERS_FILE = "users.txt";

    private Main() {
    }

    /**
     * Starts the application.
     *
     * @param args optional path of a command file; without it commands are
     *             read interactively from standard input
     */
    public static void main(String[] args) {
        System.out.println(BANNER);
        Path usersFile = Path.of(System.getProperty(USERS_FILE_PROPERTY, DEFAULT_USERS_FILE));
        UserRepository users = new UserRepository(new UserFileStore(usersFile));
        CommandProcessor processor = new CommandProcessor(new UserService(users));
        if (args.length > 0) {
            runFile(processor, args[0], System.out);
        } else {
            runStandardInput(processor, System.out);
        }
    }

    private static void runFile(CommandProcessor processor, String path, PrintStream out) {
        try (BufferedReader in = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            run(processor, in, out);
        } catch (IOException | InvalidPathException e) {
            out.println("Error: cannot read command file " + path);
        }
    }

    private static void runStandardInput(CommandProcessor processor, PrintStream out) {
        try {
            run(processor, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), out);
        } catch (IOException e) {
            out.println("Error: cannot read commands");
        }
    }

    /**
     * Single processing loop shared by every input source.
     */
    static void run(CommandProcessor processor, BufferedReader in, PrintStream out) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            String result = processor.process(line);
            if (result != null) {
                out.println(result);
            }
            if (processor.isExitRequested()) {
                return;
            }
        }
    }
}
