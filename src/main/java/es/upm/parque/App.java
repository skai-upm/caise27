package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Starts the application. Without arguments commands are read from standard input; with an argument,
 * the first one is the path of a file with one command per line. Both sources share the same processing.
 */
public class App {

    static final Path USERS_FILE = Path.of("data", "users.txt");
    static final String GROUPS_FILE_NAME = "groups.txt";
    static final String TICKETS_FILE_NAME = "tickets.txt";

    public static void main(String[] args) {
        run(args, System.in, System.out, USERS_FILE);
    }

    static void run(String[] args, InputStream stdin, PrintStream out, Path usersFile) {
        CommandProcessor processor;
        try {
            UserManager users = new UserManager(new UserStorage(usersFile));
            GroupManager groups = new GroupManager(users, new GroupStorage(usersFile.resolveSibling(GROUPS_FILE_NAME)));
            TicketManager tickets =
                new TicketManager(users, new TicketStorage(usersFile.resolveSibling(TICKETS_FILE_NAME)));
            processor = new CommandProcessor(users, groups, tickets);
        } catch (UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
            return;
        }
        if (args.length == 0) {
            runSource(processor, new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out,
                "standard input");
            return;
        }
        Path commandFile = Path.of(args[0]);
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runSource(processor, reader, out, "command file " + commandFile);
        } catch (IOException e) {
            out.println("ERROR: Cannot open command file " + commandFile + " (" + describe(e) + ")");
        }
    }

    private static void runSource(CommandProcessor processor, BufferedReader reader, PrintStream out, String name) {
        try {
            processor.run(reader, out);
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: Cannot read " + name + " (" + describe(e) + ")");
        }
    }

    private static String describe(Exception e) {
        return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
    }
}
