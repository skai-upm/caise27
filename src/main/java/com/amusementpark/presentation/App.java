package com.amusementpark.presentation;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Starts the application and reads commands from standard input or, when a path
 * is given as the first argument, from that file. With {@code --fly} all data is kept
 * in a temporary directory that is deleted on termination.
 */
public class App {
    static final Path DATA_DIR = Path.of("");
    static final String FLY_FLAG = "--fly";
    private static final DateTimeFormatter FLY_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        System.exit(run(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, DATA_DIR));
    }

    /**
     * Runs the application and returns its exit status. Persistence files live in
     * {@code baseDir}, or in a fresh {@code baseDir/tmp/<timestamp>} directory with {@code --fly}.
     */
    static int run(String[] args, BufferedReader stdin, PrintStream out, Path baseDir) {
        List<String> arguments = new ArrayList<>(List.of(args));
        boolean fly = arguments.remove(FLY_FLAG);
        if (!fly) {
            return run(arguments, stdin, out, baseDir);
        }
        Path flyDir;
        try {
            flyDir = createFlyDirectory(baseDir);
        } catch (IOException e) {
            out.println("ERROR: cannot create temporary directory: " + describe(e));
            return 1;
        }
        Thread cleanup = new Thread(() -> deleteRecursively(flyDir));
        Runtime.getRuntime().addShutdownHook(cleanup);
        try {
            out.println("Fly mode: using temporary data directory " + flyDir);
            return run(arguments, stdin, out, flyDir);
        } finally {
            deleteRecursively(flyDir);
            Runtime.getRuntime().removeShutdownHook(cleanup);
        }
    }

    private static int run(List<String> arguments, BufferedReader stdin, PrintStream out, Path dataDir) {
        CommandProcessor processor;
        try {
            processor = Bootstrap.initialize(dataDir, LocalDate.now(), out);
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: could not load data from " + dataDir.toAbsolutePath() + ": " + e.getMessage());
            return 1;
        }
        if (arguments.isEmpty()) {
            out.println("Amusement park - type HELP for the list of commands");
            return processAll(stdin, processor, out, "standard input");
        }
        String commandFile = arguments.get(0);
        try (BufferedReader file = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            return processAll(file, processor, out, commandFile);
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR: cannot open command file " + commandFile + ": " + describe(e));
            return 1;
        }
    }

    static Path createFlyDirectory(Path baseDir) throws IOException {
        Path tmp = baseDir.resolve("tmp");
        Files.createDirectories(tmp);
        String stamp = LocalDateTime.now().format(FLY_STAMP);
        Path dir = tmp.resolve(stamp);
        for (int suffix = 1; Files.exists(dir); suffix++) {
            dir = tmp.resolve(stamp + "_" + suffix);
        }
        return Files.createDirectory(dir);
    }

    private static void deleteRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException | UncheckedIOException e) {
            System.err.println("WARNING: could not delete temporary directory " + dir + ": " + e.getMessage());
        }
    }

    private static int processAll(BufferedReader reader, CommandProcessor processor,
                                  PrintStream out, String source) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!processor.process(line)) {
                    break;
                }
            }
            return 0;
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: cannot read " + source + ": " + describe(e));
            return 1;
        }
    }

    private static String describe(Exception e) {
        if (e instanceof NoSuchFileException) {
            return "file does not exist";
        }
        if (e instanceof AccessDeniedException) {
            return "permission denied";
        }
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
