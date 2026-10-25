package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Starts the application. Without arguments commands are read from standard input;
 * with an argument they are read from the file it names. Both modes share the same processing.
 * The --fly option redirects all persistence to a temporary directory deleted at the end.
 */
public class App {

    static final Path DATA_DIR = Path.of("data");
    static final Path TMP_DIR = Path.of("tmp");
    static final String FLY_OPTION = "--fly";

    public static void main(String[] args) {
        launch(args, System.in, System.out, DATA_DIR, TMP_DIR, LocalDateTime.now());
    }

    /**
     * Separates the --fly option from the positional arguments, chooses the persistence directory
     * and runs the application, deleting the temporary directory when it ends.
     */
    static void launch(String[] args, InputStream stdin, PrintStream out, Path dataDir, Path tmpBase,
                       LocalDateTime now) {
        List<String> positional = new ArrayList<>(Arrays.asList(args));
        boolean fly = positional.removeIf(FLY_OPTION::equals);
        String[] rest = positional.toArray(new String[0]);
        if (!fly) {
            start(rest, stdin, out, dataDir);
            return;
        }
        Path flyDir;
        try {
            flyDir = TemporaryDirectory.create(tmpBase, now);
        } catch (IOException e) {
            out.println("ERROR CANNOT CREATE TEMPORARY DIRECTORY");
            return;
        }
        Thread cleanup = new Thread(() -> deleteQuietly(flyDir));
        Runtime.getRuntime().addShutdownHook(cleanup);
        try {
            start(rest, stdin, out, flyDir);
        } finally {
            deleteQuietly(flyDir);
            Runtime.getRuntime().removeShutdownHook(cleanup);
        }
    }

    private static void deleteQuietly(Path dir) {
        try {
            TemporaryDirectory.deleteRecursively(dir);
        } catch (IOException e) {
            // Best effort: the directory lives under tmp/, which is never read by normal runs.
        }
    }

    static void start(String[] args, InputStream stdin, PrintStream out, Path dataDir) {
        CommandProcessor processor;
        try {
            UserManager users = new UserManager(new UserRepository(dataDir.resolve("users.txt")));
            GroupManager groups = new GroupManager(new GroupRepository(dataDir.resolve("groups.txt")), users);
            SystemDate systemDate = new SystemDate(LocalDate.now());
            TicketManager tickets = new TicketManager(new TicketRepository(dataDir.resolve("tickets.txt")),
                    new GroupTicketRepository(dataDir.resolve("grouptickets.txt")), users, groups, systemDate);
            AttractionManager attractions = new AttractionManager(
                    new AttractionRepository(dataDir.resolve("attractions.txt")), tickets, systemDate);
            processor = new CommandProcessor(users, groups, tickets, attractions, systemDate);
        } catch (IOException e) {
            out.println("ERROR " + e.getMessage());
            return;
        }
        if (args.length == 0) {
            try {
                run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out, processor);
            } catch (IOException e) {
                out.println("ERROR CANNOT READ STANDARD INPUT");
            }
            return;
        }
        String path = args[0];
        try (BufferedReader file = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            run(file, out, processor);
        } catch (IOException | UncheckedIOException | InvalidPathException e) {
            out.println("ERROR CANNOT READ FILE " + path);
        }
    }

    static void run(BufferedReader in, PrintStream out, CommandProcessor processor) throws IOException {
        String line;
        while (!processor.isFinished() && (line = in.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
