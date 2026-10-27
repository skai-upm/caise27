package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Application entry point. Reads commands from standard input, or from the
 * file given as the first argument, and processes them one per line. With
 * {@code --fly} as first argument all data is kept in a temporary directory
 * that is deleted at the end of the execution.
 */
public class App {

    static final Path DATA_DIR = Path.of("data");
    static final Path FLY_BASE_DIR = Path.of("tmp");
    static final String FLY = "--fly";
    static final String USERS_FILE = "users.txt";
    static final String GROUPS_FILE = "groups.txt";
    static final String TICKETS_FILE = "tickets.txt";
    static final String ATTRACTIONS_FILE = "attractions.txt";

    private static final DateTimeFormatter FLY_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        run(args, System.in, System.out, DATA_DIR, FLY_BASE_DIR);
    }

    static void run(String[] args, InputStream in, PrintStream out, Path dataDir) {
        run(args, in, out, dataDir, dataDir.resolveSibling("tmp"));
    }

    static void run(String[] args, InputStream in, PrintStream out, Path dataDir, Path flyBaseDir) {
        if (args.length > 0 && args[0].equals(FLY)) {
            Path flyDir;
            try {
                flyDir = createFlyDirectory(flyBaseDir);
            } catch (IOException e) {
                out.println("ERROR: could not create temporary directory: " + e.getMessage());
                return;
            }
            try {
                runWithData(Arrays.copyOfRange(args, 1, args.length), in, out, flyDir);
            } finally {
                deleteFlyDirectory(flyDir, flyBaseDir, out);
            }
            return;
        }
        runWithData(args, in, out, dataDir);
    }

    private static void runWithData(String[] args, InputStream in, PrintStream out, Path dataDir) {
        if (!loadPark(dataDir, out)) {
            return;
        }
        UserRepository users = ParkController.getInstance().getUsers();
        CommandRunner runner = new CommandRunner(new CommandProcessor(out, new UserService(users)));
        if (args.length == 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                runner.run(reader);
            } catch (IOException e) {
                out.println("ERROR: could not read standard input: " + e.getMessage());
            }
            return;
        }
        Path commandFile = Path.of(args[0]);
        if (!Files.exists(commandFile)) {
            out.println("ERROR: command file not found: " + args[0]);
            return;
        }
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            out.println("ERROR: command file cannot be read: " + args[0]);
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            out.println("ERROR: command file cannot be read: " + args[0]);
        }
    }

    /**
     * Loads the park state stored in the data directory and makes it the
     * current state of the {@link ParkController}.
     *
     * @return {@code false} if the stored data could not be read
     */
    private static boolean loadPark(Path dataDir, PrintStream out) {
        try {
            UserFileStorage userStorage = new UserFileStorage(dataDir.resolve(USERS_FILE));
            UserRepository users = new UserRepository(userStorage);
            GroupFileStorage groupStorage = new GroupFileStorage(dataDir.resolve(GROUPS_FILE));
            GroupRepository groups = new GroupRepository(groupStorage, users);
            TicketFileStorage ticketStorage = new TicketFileStorage(dataDir.resolve(TICKETS_FILE));
            TicketRepository tickets = new TicketRepository(ticketStorage, users, groups);
            AttractionFileStorage attractionStorage = new AttractionFileStorage(dataDir.resolve(ATTRACTIONS_FILE));
            AttractionRepository attractions = new AttractionRepository(attractionStorage);
            int skipped = userStorage.getSkippedLines() + groupStorage.getSkippedLines()
                    + ticketStorage.getSkippedLines() + attractionStorage.getSkippedLines();
            if (skipped > 0) {
                out.println("WARNING: " + skipped + " malformed line(s) in the data files were ignored.");
            }
            ParkController.getInstance().initialize(users, groups, tickets, attractions, LocalDate.now());
            return true;
        } catch (UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
            return false;
        }
    }

    private static Path createFlyDirectory(Path baseDir) throws IOException {
        String stamp = LocalDateTime.now().format(FLY_DIR_FORMAT);
        Path dir = baseDir.resolve(stamp);
        for (int i = 1; Files.exists(dir); i++) {
            dir = baseDir.resolve(stamp + "_" + i);
        }
        return Files.createDirectories(dir);
    }

    private static void deleteFlyDirectory(Path flyDir, Path baseDir, PrintStream out) {
        try (Stream<Path> paths = Files.walk(flyDir)) {
            List<Path> all = paths.sorted(Comparator.reverseOrder()).toList();
            for (Path path : all) {
                Files.deleteIfExists(path);
            }
            try (Stream<Path> rest = Files.list(baseDir)) {
                if (rest.findAny().isEmpty()) {
                    Files.deleteIfExists(baseDir);
                }
            }
        } catch (IOException e) {
            out.println("ERROR: could not delete temporary directory " + flyDir + ": " + e.getMessage());
        }
    }
}
