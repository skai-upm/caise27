package es.upm.etsisi.amusementpark;

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
 * Entry point. Without arguments, commands are read from standard input
 * (interactive mode); with an argument, the first one is the path of a command
 * file (file mode). Both modes share the same {@link CommandRunner}.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");
    static final String GROUPS_FILE_NAME = "groups.txt";
    static final String TICKETS_FILE_NAME = "tickets.txt";
    static final String GROUP_TICKETS_FILE_NAME = "grouptickets.txt";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application.
     *
     * @param usersFile users file; the groups, tickets and group tickets files ({@value #GROUPS_FILE_NAME},
     *                  {@value #TICKETS_FILE_NAME}, {@value #GROUP_TICKETS_FILE_NAME}) are in the same directory
     * @return 0 on normal termination, 1 if the stored data or the command file could not be read
     */
    static int run(String[] args, InputStream stdin, PrintStream out, Path usersFile) {
        CommandRunner runner;
        try {
            UserManager users = new UserManager(new UserFileRepository(usersFile));
            GroupManager groups = new GroupManager(
                    new GroupFileRepository(usersFile.resolveSibling(GROUPS_FILE_NAME)), users);
            TicketManager tickets = new TicketManager(
                    new TicketFileRepository(usersFile.resolveSibling(TICKETS_FILE_NAME)),
                    new GroupTicketFileRepository(usersFile.resolveSibling(GROUP_TICKETS_FILE_NAME)), users, groups);
            runner = new CommandRunner(new CommandProcessor(users, groups, tickets));
        } catch (IOException e) {
            out.println("ERROR: Could not load stored data next to '" + usersFile + "': " + e);
            return 1;
        }
        if (args.length == 0) {
            BufferedReader in = new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8));
            return runner.run(in, out) ? 0 : 1;
        }
        String file = args[0];
        try (BufferedReader in = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
            return runner.run(in, out) ? 0 : 1;
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR: Cannot open command file '" + file + "': " + e);
            return 1;
        }
    }
}
