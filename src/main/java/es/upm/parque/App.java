package es.upm.parque;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

import es.upm.parque.business.security.BCryptPasswordEncoder;
import es.upm.parque.business.security.PasswordEncoder;
import es.upm.parque.persistence.StoragePaths;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.ConsoleRunner;

/**
 * Entry point of the Amusement Park application. Without a file argument it reads commands from standard input;
 * with one it reads them from that file. With {@code --fly} all data is kept in a temporary directory that is
 * deleted at the end of the run.
 */
public class App {

    static final String FLY_OPTION = "--fly";
    static final String DATA_DIRECTORY = "data";
    static final String FLY_DIRECTORY = "tmp";

    private static final DateTimeFormatter FLY_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        int code = run(args, System.in, System.out, Path.of(""));
        if (code != 0) {
            System.exit(code);
        }
    }

    /**
     * Runs the application with data directories relative to {@code baseDir} and returns the exit code.
     */
    static int run(String[] args, InputStream stdin, PrintStream out, Path baseDir) {
        return run(args, stdin, out, baseDir, new BCryptPasswordEncoder());
    }

    static int run(String[] args, InputStream stdin, PrintStream out, Path baseDir, PasswordEncoder encoder) {
        boolean fly = false;
        String commandFile = null;
        for (String arg : args) {
            if (FLY_OPTION.equals(arg)) {
                fly = true;
            } else if (commandFile == null) {
                commandFile = arg;
            } else {
                out.println("ERROR Too many arguments. Usage: [--fly] [command-file]");
                return 1;
            }
        }
        if (!fly) {
            return execute(commandFile, stdin, out, new StoragePaths(baseDir.resolve(DATA_DIRECTORY)), encoder);
        }

        Path flyDirectory;
        try {
            flyDirectory = createFlyDirectory(baseDir.resolve(FLY_DIRECTORY));
        } catch (IOException e) {
            out.println("ERROR Cannot create temporary directory: " + e.getMessage());
            return 1;
        }
        Thread cleanup = new Thread(() -> deleteRecursively(flyDirectory));
        Runtime.getRuntime().addShutdownHook(cleanup);
        try {
            out.println("Fly mode: using temporary directory " + flyDirectory);
            return execute(commandFile, stdin, out, new StoragePaths(flyDirectory), encoder);
        } finally {
            deleteRecursively(flyDirectory);
            try {
                Runtime.getRuntime().removeShutdownHook(cleanup);
            } catch (IllegalStateException e) {
                // The JVM is already shutting down; the hook will run anyway.
            }
        }
    }

    private static int execute(String commandFile, InputStream stdin, PrintStream out, StoragePaths paths,
            PasswordEncoder encoder) {
        CommandProcessor processor;
        try {
            processor = ParkApplication.start(paths, LocalDate.now(), encoder);
        } catch (IOException e) {
            out.println("ERROR Cannot load data from " + paths.getDirectory() + ": " + e.getMessage());
            return 1;
        }
        ConsoleRunner runner = new ConsoleRunner(processor, out);
        if (commandFile == null) {
            out.println("Amusement Park - type HELP to see the available commands");
            return runner.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)),
                    "standard input");
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            return runner.run(reader, commandFile);
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR Cannot read command file: " + commandFile);
            return 1;
        }
    }

    /**
     * Creates {@code parent/yyyyMMdd_HHmmss}, adding a numeric suffix if that directory already exists.
     */
    static Path createFlyDirectory(Path parent) throws IOException {
        Files.createDirectories(parent);
        String name = LocalDateTime.now().format(FLY_TIMESTAMP);
        for (int attempt = 0;; attempt++) {
            Path candidate = parent.resolve(attempt == 0 ? name : name + "_" + attempt);
            try {
                return Files.createDirectory(candidate);
            } catch (FileAlreadyExistsException e) {
                // try the next suffix
            }
        }
    }

    static void deleteRecursively(Path directory) {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    System.err.println("WARNING Could not delete " + path);
                }
            });
        } catch (IOException e) {
            System.err.println("WARNING Could not delete " + directory);
        }
    }
}
