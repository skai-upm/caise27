package com.parque;

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
import java.util.stream.Stream;

/**
 * Entry point. Without a file argument commands are read from standard input; otherwise the first argument
 * (other than {@code --fly}) is the path of a command file. Both sources go through the same loop and the same
 * CommandProcessor. With {@code --fly} all persistence goes to a per-execution temporary directory
 * {@code tmp/<yyyyMMdd_HHmmss>/} that is deleted when the application terminates.
 */
public class App {
    /** Data files live in the working directory. */
    static final Path DATA_DIR = Path.of("");
    static final String FLY_OPTION = "--fly";
    static final String FLY_ROOT = "tmp";
    static final String USERS_FILE = "users.txt";
    static final String GROUPS_FILE = "groups.txt";
    static final String TICKETS_FILE = "tickets.txt";
    static final String GROUP_TICKETS_FILE = "grouptickets.txt";
    static final String ATTRACTIONS_FILE = "attractions.txt";

    private static final DateTimeFormatter FLY_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, DATA_DIR, true);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application and returns the process exit status (0 normal end, 1 controlled failure).
     */
    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path baseDir) {
        return run(args, stdin, out, err, baseDir, false);
    }

    private static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path baseDir,
            boolean registerShutdownHook) {
        boolean fly = false;
        List<String> rest = new ArrayList<>();
        for (String a : args) {
            if (a.equals(FLY_OPTION)) {
                fly = true;
            } else {
                rest.add(a);
            }
        }
        String commandFile = rest.isEmpty() ? null : rest.get(0);
        if (!fly) {
            return runWithDataDir(commandFile, stdin, out, err, baseDir);
        }
        Path flyDir;
        try {
            flyDir = createFlyDirectory(baseDir, LocalDateTime.now());
        } catch (IOException e) {
            err.println("ERROR: Cannot create temporary directory for --fly: " + describe(e));
            return 1;
        }
        Thread cleanup = new Thread(() -> deleteFlyDirectory(flyDir, err));
        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(cleanup);
        }
        try {
            return runWithDataDir(commandFile, stdin, out, err, flyDir);
        } finally {
            deleteFlyDirectory(flyDir, err);
            if (registerShutdownHook) {
                try {
                    Runtime.getRuntime().removeShutdownHook(cleanup);
                } catch (IllegalStateException alreadyShuttingDown) {
                    // the hook is running or will run; deletion is idempotent
                }
            }
        }
    }

    /** Wires every repository to {@code dataDir} and processes the commands. */
    private static int runWithDataDir(String commandFile, InputStream stdin, PrintStream out, PrintStream err,
            Path dataDir) {
        CommandProcessor processor;
        try {
            UserService users = new UserService(new UserRepository(dataDir.resolve(USERS_FILE)));
            GroupService groups = new GroupService(new GroupRepository(dataDir.resolve(GROUPS_FILE)), users);
            TicketService tickets = new TicketService(new TicketRepository(dataDir.resolve(TICKETS_FILE)),
                    new GroupTicketRepository(dataDir.resolve(GROUP_TICKETS_FILE)), users, groups);
            AttractionService attractions = new AttractionService(new AttractionRepository(dataDir.resolve(ATTRACTIONS_FILE)));
            processor = new CommandProcessor(users, groups, tickets, attractions);
        } catch (IOException e) {
            err.println("ERROR: Cannot load data from '" + dataDir.toAbsolutePath() + "': " + describe(e));
            return 1;
        }
        if (commandFile == null) {
            try {
                processLines(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out, processor);
            } catch (IOException e) {
                err.println("ERROR: Cannot read standard input: " + describe(e));
                return 1;
            }
            return 0;
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            processLines(reader, out, processor);
        } catch (IOException | InvalidPathException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + describe(e));
            return 1;
        }
        return 0;
    }

    /**
     * Creates a new, empty {@code base/tmp/yyyyMMdd_HHmmss} directory for this execution, adding {@code _1},
     * {@code _2}... if a directory with that timestamp already exists.
     */
    static Path createFlyDirectory(Path base, LocalDateTime start) throws IOException {
        Path root = base.resolve(FLY_ROOT);
        Files.createDirectories(root);
        String stamp = start.format(FLY_STAMP);
        for (int i = 0;; i++) {
            Path candidate = root.resolve(i == 0 ? stamp : stamp + "_" + i);
            try {
                return Files.createDirectory(candidate);
            } catch (FileAlreadyExistsException taken) {
                // try the next suffix
            }
        }
    }

    /** Deletes the run directory and its files, and the {@code tmp} parent if it is left empty. Idempotent. */
    static void deleteFlyDirectory(Path dir, PrintStream err) {
        try {
            if (Files.exists(dir)) {
                try (Stream<Path> walk = Files.walk(dir)) {
                    for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                        Files.deleteIfExists(p);
                    }
                }
            }
            Path root = dir.toAbsolutePath().getParent();
            if (root != null && Files.isDirectory(root)) {
                try (Stream<Path> children = Files.list(root)) {
                    if (children.findAny().isEmpty()) {
                        Files.deleteIfExists(root);
                    }
                }
            }
        } catch (IOException e) {
            err.println("ERROR: Cannot delete temporary directory '" + dir + "': " + describe(e));
        }
    }

    /** The single command loop shared by interactive and file mode. */
    private static void processLines(BufferedReader reader, PrintStream out, CommandProcessor processor)
            throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            out.println(processor.process(line));
        }
    }

    private static String describe(Exception e) {
        String kind = e.getClass().getSimpleName();
        return e.getMessage() == null ? kind : kind + " (" + e.getMessage() + ")";
    }
}
