package com.amusementpark;

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
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Application entry point. Without a command file, commands are read from standard input;
 * otherwise from the given file. A leading {@code --fly} keeps all persistence in a temporary
 * directory that is deleted when the run ends.
 */
public class App {

    static final String FLY_FLAG = "--fly";

    private static final DateTimeFormatter FLY_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, Path.of(""));
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application with the given streams; persistence files live in {@code workDir}
     * (normal mode) or in a new {@code workDir/tmp/<timestamp>} directory ({@code --fly}).
     *
     * @return 0 on normal termination, 1 if data or the command file could not be read
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path workDir) {
        boolean fly = args.length > 0 && FLY_FLAG.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;
        if (!fly) {
            return runWith(StoragePaths.in(workDir), commandFile, in, out, err);
        }
        Path flyDir;
        try {
            flyDir = createFlyDirectory(workDir.resolve("tmp"));
        } catch (IOException e) {
            err.println("ERROR: Could not create temporary directory: " + e.getMessage());
            return 1;
        }
        Thread cleanup = new Thread(() -> deleteRecursively(flyDir));
        Runtime.getRuntime().addShutdownHook(cleanup);
        try {
            return runWith(StoragePaths.in(flyDir), commandFile, in, out, err);
        } finally {
            deleteRecursively(flyDir);
            try {
                Runtime.getRuntime().removeShutdownHook(cleanup);
            } catch (IllegalStateException e) {
                // JVM already shutting down: the hook runs anyway.
            }
        }
    }

    private static int runWith(StoragePaths paths, String commandFile, InputStream in, PrintStream out,
                               PrintStream err) {
        ParkController park;
        try {
            park = new ParkController(paths);
        } catch (IOException e) {
            err.println("ERROR: Could not read data files: " + e.getMessage());
            return 1;
        }
        CommandProcessor processor = new CommandProcessor(park, out);
        if (commandFile == null) {
            return runInteractive(processor, in, err);
        }
        return runFile(processor, commandFile, err);
    }

    private static int runInteractive(CommandProcessor processor, InputStream in, PrintStream err) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            processor.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: Could not read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandProcessor processor, String fileName, PrintStream err) {
        Path path;
        try {
            path = Path.of(fileName);
        } catch (InvalidPathException e) {
            err.println("ERROR: Invalid command file path: " + fileName);
            return 1;
        }
        if (!Files.isRegularFile(path)) {
            err.println("ERROR: Command file not found: " + fileName);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            processor.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: Could not read command file " + fileName + ": " + e.getMessage());
            return 1;
        }
    }

    /** Creates {@code tmpRoot/<yyyyMMdd_HHmmss>}, adding a numeric suffix if that name is taken. */
    static Path createFlyDirectory(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String stamp = LocalDateTime.now().format(FLY_DIR_FORMAT);
        Path dir = tmpRoot.resolve(stamp);
        for (int n = 1; Files.exists(dir); n++) {
            dir = tmpRoot.resolve(stamp + "_" + n);
        }
        return Files.createDirectory(dir);
    }

    static void deleteRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException | UncheckedIOException e) {
            System.err.println("WARNING: Could not delete temporary directory " + dir + ": " + e.getMessage());
        }
    }
}
