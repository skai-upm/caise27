package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Entry point: starts the application and handles terminal interaction.
 * Without arguments commands are read from standard input; with an argument, the first one is the
 * path of a text file with one command per line. Both sources share the same processing loop.
 * Data files ({@code users.txt}, {@code groups.txt}, {@code tickets.txt}, {@code grouptickets.txt}, {@code attractions.txt}) are kept in the working directory.
 * With the {@code --fly} option they are kept instead in a new {@code tmp/yyyyMMdd_HHmmss/} directory that is
 * deleted when the application terminates, so the permanent data is never modified.
 */
public class App {

    /** Launch option selecting the temporary persistence mode. */
    static final String FLY_OPTION = "--fly";
    private static final DateTimeFormatter TMP_NAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        execute(args, System.in, System.out, Path.of("."));
    }

    /**
     * Runs the application for the given launch arguments: {@code --fly} (anywhere) selects temporary
     * persistence, and the first other argument, if any, is the command file.
     */
    static void execute(String[] args, InputStream in, PrintStream out, Path workDir) {
        boolean fly = Arrays.asList(args).contains(FLY_OPTION);
        List<String> rest = Arrays.stream(args).filter(a -> !a.equals(FLY_OPTION)).toList();
        if (!fly) {
            runSelectedMode(rest, in, out, workDir, workDir);
            return;
        }
        Path tmpRoot = workDir.resolve("tmp");
        Path dataDir;
        try {
            dataDir = createTemporaryDataDir(tmpRoot, LocalDateTime.now());
        } catch (IOException e) {
            out.println("ERROR: Cannot create temporary data directory: " + e.getMessage());
            return;
        }
        Thread cleanup = new Thread(() -> deleteTemporaryDataDir(dataDir, tmpRoot));
        Runtime.getRuntime().addShutdownHook(cleanup);
        try {
            runSelectedMode(rest, in, out, workDir, dataDir);
        } finally {
            deleteTemporaryDataDir(dataDir, tmpRoot);
            try {
                Runtime.getRuntime().removeShutdownHook(cleanup);
            } catch (IllegalStateException e) {
                // the JVM is already shutting down: the hook runs anyway and deletion is idempotent
            }
        }
    }

    private static void runSelectedMode(List<String> rest, InputStream in, PrintStream out, Path workDir,
                                        Path dataDir) {
        if (rest.isEmpty()) {
            runStandardInput(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), out, dataDir);
        } else {
            runFile(workDir.resolve(rest.get(0)), out, dataDir);
        }
    }

    /** Creates {@code tmpRoot/yyyyMMdd_HHmmss}, adding {@code _2}, {@code _3}... if that name is taken. */
    static Path createTemporaryDataDir(Path tmpRoot, LocalDateTime start) throws IOException {
        Files.createDirectories(tmpRoot);
        String name = TMP_NAME.format(start);
        Path dir = tmpRoot.resolve(name);
        for (int n = 2; Files.exists(dir); n++) {
            dir = tmpRoot.resolve(name + "_" + n);
        }
        return Files.createDirectory(dir);
    }

    /** Deletes the run's temporary directory with all its files, and {@code tmpRoot} if it is left empty. */
    static void deleteTemporaryDataDir(Path dataDir, Path tmpRoot) {
        try {
            if (Files.exists(dataDir)) {
                try (Stream<Path> paths = Files.walk(dataDir)) {
                    for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                        Files.deleteIfExists(p);
                    }
                }
            }
            if (Files.isDirectory(tmpRoot)) {
                try (Stream<Path> left = Files.list(tmpRoot)) {
                    if (left.findAny().isEmpty()) {
                        Files.deleteIfExists(tmpRoot);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot delete temporary data directory " + dataDir + ": " + e.getMessage());
        }
    }

    /** Runs the commands typed on standard input. */
    static void runStandardInput(BufferedReader in, PrintStream out, Path dataDir) {
        try {
            run(in, out, dataDir);
        } catch (IOException e) {
            out.println("ERROR: Cannot read standard input: " + e.getMessage());
        }
    }

    /** Runs the commands of a file, reporting a missing or unreadable file without an exception. */
    static void runFile(Path commandFile, PrintStream out, Path dataDir) {
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            out.println("ERROR: Cannot read command file " + commandFile + ": it does not exist or is not a readable file");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            run(reader, out, dataDir);
        } catch (IOException e) {
            out.println("ERROR: Cannot read command file " + commandFile + ": " + e.getMessage());
        }
    }

    /** Builds the controller and its services over the data files kept in {@code dataDir}. */
    static CommandProcessor createProcessor(Path dataDir) {
        UserService users = new UserService(new UserFileStorage(dataDir.resolve(UserFileStorage.DEFAULT_FILE)));
        GroupService groups = new GroupService(new GroupFileStorage(dataDir.resolve(GroupFileStorage.DEFAULT_FILE)), users);
        TicketService tickets = new TicketService(new TicketFileStorage(dataDir.resolve(TicketFileStorage.DEFAULT_FILE)),
                new GroupTicketFileStorage(dataDir.resolve(GroupTicketFileStorage.DEFAULT_FILE)), users, groups);
        AttractionService attractions =
                new AttractionService(new AttractionFileStorage(dataDir.resolve(AttractionFileStorage.DEFAULT_FILE)));
        return new CommandProcessor(users, groups, tickets, attractions);
    }

    /**
     * Shared command loop for both input modes: processes each non-blank line in order and stops
     * after EXIT or at the end of the input.
     */
    static void run(BufferedReader reader, PrintStream out, Path dataDir) throws IOException {
        CommandProcessor processor = createProcessor(dataDir);
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
