package es.upm.park;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Application entry point. Without a command-file argument commands are read from
 * standard input; otherwise the first argument is the path of a command file. Both
 * modes share the same {@link CommandRunner}. The {@code --fly} option redirects all
 * persistence files to a temporary per-run directory that is deleted at the end.
 */
public class App {

    static final String FLY_OPTION = "--fly";
    static final String TMP_DIR = "tmp";

    public static void main(String[] args) {
        int status = launch(args, Path.of(""), LocalDateTime.now(), System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Locations of the persistence files. */
    record DataFiles(Path users, Path groups, Path tickets, Path groupTickets, Path attractions) {

        /** The standard file names inside {@code dir}. */
        static DataFiles in(Path dir) {
            return new DataFiles(dir.resolve("users.txt"), dir.resolve("groups.txt"), dir.resolve("tickets.txt"),
                    dir.resolve("group-tickets.txt"), dir.resolve("attractions.txt"));
        }
    }

    /**
     * Interprets the launch arguments: {@code --fly} selects temporary persistence under
     * {@code workDir/tmp}; the remaining arguments are passed to {@link #run}.
     */
    static int launch(String[] args, Path workDir, LocalDateTime start, InputStream stdin, PrintStream out,
                      PrintStream err) {
        List<String> rest = new ArrayList<>();
        boolean fly = false;
        for (String a : args) {
            if (FLY_OPTION.equals(a)) {
                fly = true;
            } else {
                rest.add(a);
            }
        }
        String[] commandArgs = rest.toArray(new String[0]);
        if (!fly) {
            return run(commandArgs, DataFiles.in(workDir), stdin, out, err);
        }
        Path runDir;
        try {
            runDir = TemporaryDataDirectory.create(workDir.resolve(TMP_DIR), start);
        } catch (IOException e) {
            err.println("ERROR: cannot create temporary directory: " + e.getMessage());
            return 1;
        }
        Thread cleanupOnShutdown = new Thread(() -> deleteQuietly(runDir, err));
        Runtime.getRuntime().addShutdownHook(cleanupOnShutdown);
        try {
            return run(commandArgs, DataFiles.in(runDir), stdin, out, err);
        } finally {
            deleteQuietly(runDir, err);
            removeHook(cleanupOnShutdown);
        }
    }

    private static void deleteQuietly(Path dir, PrintStream err) {
        try {
            TemporaryDataDirectory.deleteRecursively(dir);
        } catch (IOException e) {
            err.println("ERROR: cannot delete temporary directory " + dir + ": " + e.getMessage());
        }
    }

    private static void removeHook(Thread hook) {
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException e) {
            // JVM already shutting down: the hook itself performs the cleanup.
        }
    }

    /** Runs the application with the given data files and returns its exit status (0 on success). */
    static int run(String[] args, DataFiles files, InputStream stdin, PrintStream out, PrintStream err) {
        CommandRunner runner;
        try {
            UserManager users = new UserManager(new UserFileStorage(files.users()));
            GroupManager groups = new GroupManager(new GroupFileStorage(files.groups()), users);
            TicketManager tickets = new TicketManager(new TicketFileStorage(files.tickets()),
                    new GroupTicketFileStorage(files.groupTickets()), users, groups);
            AttractionManager attractions = new AttractionManager(new AttractionFileStorage(files.attractions()));
            runner = new CommandRunner(new CommandProcessor(users, groups, tickets, attractions));
        } catch (UncheckedIOException e) {
            err.println("ERROR: cannot load data: " + e.getMessage());
            return 1;
        }
        if (args.length == 0) {
            return runInteractive(runner, stdin, out, err);
        }
        return runFile(runner, args[0], out, err);
    }

    private static int runInteractive(CommandRunner runner, InputStream stdin, PrintStream out, PrintStream err) {
        try {
            runner.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandRunner runner, String fileArg, PrintStream out, PrintStream err) {
        String prefix = "ERROR: cannot read command file '" + fileArg + "': ";
        Path file;
        try {
            file = Path.of(fileArg);
        } catch (InvalidPathException e) {
            err.println(prefix + "invalid path");
            return 1;
        }
        if (!Files.exists(file)) {
            err.println(prefix + "file does not exist");
            return 1;
        }
        if (!Files.isRegularFile(file)) {
            err.println(prefix + "not a regular file");
            return 1;
        }
        if (!Files.isReadable(file)) {
            err.println(prefix + "file is not readable");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            runner.run(reader, out);
            return 0;
        } catch (IOException | UncheckedIOException e) {
            err.println(prefix + e.getMessage());
            return 1;
        }
    }
}
