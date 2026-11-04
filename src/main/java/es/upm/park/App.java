package es.upm.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Entry point. Without a command-file argument commands are read from standard input;
 * otherwise the first argument other than {@code --fly} is the path of a command file. Both
 * sources share the same processing. With {@code --fly} all data files live in a temporary,
 * per-run directory that is deleted when the run ends.
 */
public class App {

    /** Data files live in the working directory. */
    static final Path DATA_DIR = Path.of("");
    /** Root of the temporary run directories used by {@code --fly}. */
    static final Path TMP_ROOT = Path.of("tmp");
    static final String FLY_FLAG = "--fly";
    private static final DateTimeFormatter RUN_NAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** Temporary directory of the current fly run, for cleanup on abnormal termination. */
    private static final AtomicReference<Path> ACTIVE_FLY_DIR = new AtomicReference<>();

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Path dir = ACTIVE_FLY_DIR.getAndSet(null);
            if (dir != null) {
                deleteQuietly(dir);
            }
        }));
        int status = run(args, System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Runs the application and returns its exit status (0 = normal end). */
    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err) {
        return run(args, stdin, out, err, DATA_DIR, TMP_ROOT);
    }

    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path dataDir) {
        return run(args, stdin, out, err, dataDir, TMP_ROOT);
    }

    /** Runs with the persistent data in {@code dataDir}, or in a new directory under {@code tmpRoot} with --fly. */
    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path dataDir, Path tmpRoot) {
        List<String> commandArgs = new ArrayList<>(List.of(args));
        boolean fly = commandArgs.removeIf(FLY_FLAG::equals);
        String[] rest = commandArgs.toArray(String[]::new);
        if (!fly) {
            return execute(rest, stdin, out, err, dataDir);
        }
        Path runDir;
        try {
            runDir = createRunDirectory(tmpRoot);
        } catch (IOException e) {
            err.println("ERROR: Cannot create temporary directory under '" + tmpRoot + "': " + describe(e));
            return 1;
        }
        ACTIVE_FLY_DIR.set(runDir);
        err.println("INFO: --fly mode, temporary data in " + runDir);
        try {
            return execute(rest, stdin, out, err, runDir);
        } finally {
            deleteQuietly(runDir);
            ACTIVE_FLY_DIR.compareAndSet(runDir, null);
        }
    }

    /** Creates {@code tmpRoot/yyyyMMdd_HHmmss}, adding a numeric suffix if that name is taken. */
    static Path createRunDirectory(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String base = LocalDateTime.now().format(RUN_NAME);
        for (int attempt = 0; ; attempt++) {
            Path candidate = tmpRoot.resolve(attempt == 0 ? base : base + "_" + attempt);
            try {
                return Files.createDirectory(candidate);
            } catch (FileAlreadyExistsException e) {
                // another run started in the same second: try the next suffix
            }
        }
    }

    /** Deletes a directory tree, deepest entries first; problems are reported but not thrown. */
    static void deleteQuietly(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            System.err.println("WARNING: Could not delete temporary directory " + dir + ": " + e.getMessage());
        }
    }

    /** Runs with the data files ({@code users.txt}, {@code groups.txt}, {@code tickets.txt},
     *  {@code grouptickets.txt}, {@code attractions.txt}) inside {@code dataDir}. */
    private static int execute(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path dataDir) {
        CommandRunner runner;
        try {
            UserManager users = new UserManager(new UserFileStore(dataDir.resolve("users.txt")));
            GroupManager groups = new GroupManager(new GroupFileStore(dataDir.resolve("groups.txt")), users);
            TicketManager tickets = new TicketManager(new TicketFileStore(dataDir.resolve("tickets.txt")),
                    new GroupTicketFileStore(dataDir.resolve("grouptickets.txt")), users, groups);
            AttractionManager attractions = new AttractionManager(
                    new AttractionFileStore(dataDir.resolve("attractions.txt")));
            runner = new CommandRunner(new CommandProcessor(users, groups, tickets, attractions));
        } catch (IOException e) {
            err.println("ERROR: Cannot load data files: " + describe(e));
            return 1;
        }
        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            } catch (IOException e) {
                err.println("ERROR: Cannot read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }
        String commandFile = args[0];
        Path path;
        try {
            path = Path.of(commandFile);
        } catch (InvalidPathException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + describe(e));
            return 1;
        }
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            runner.run(in, out);
        } catch (IOException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + describe(e));
            return 1;
        }
        return 0;
    }

    private static String describe(Exception e) {
        String reason = e.getMessage();
        return e.getClass().getSimpleName() + (reason == null ? "" : " (" + reason + ")");
    }
}
