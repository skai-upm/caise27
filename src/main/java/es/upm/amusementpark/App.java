package es.upm.amusementpark;

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
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Application entry point. Without a file argument commands are read from standard
 * input; otherwise the first argument other than {@code --fly} is a command file.
 * With {@code --fly} all persistence goes to a temporary directory removed at the end.
 */
public class App {

    static final String FLY_FLAG = "--fly";
    private static final DateTimeFormatter FLY_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        run(args, System.in, System.out, Path.of(""));
    }

    /**
     * Runs the application.
     *
     * @param workDir directory holding the permanent data files; {@code tmp/} is created inside it for --fly
     */
    static void run(String[] args, InputStream in, PrintStream out, Path workDir) {
        boolean fly = false;
        String commandFile = null;
        for (String arg : args) {
            if (arg.equals(FLY_FLAG)) {
                fly = true;
            } else if (commandFile == null) {
                commandFile = arg;
            }
        }

        if (!fly) {
            execute(new StoragePaths(workDir), commandFile, in, out);
            return;
        }

        Path flyDir;
        try {
            flyDir = createFlyDirectory(workDir.resolve("tmp"));
        } catch (IOException e) {
            out.println("Error: could not create temporary directory: " + e.getMessage());
            return;
        }
        Thread cleanupHook = new Thread(() -> deleteRecursively(flyDir));
        Runtime.getRuntime().addShutdownHook(cleanupHook);
        try {
            execute(new StoragePaths(flyDir), commandFile, in, out);
        } finally {
            deleteRecursively(flyDir);
            try {
                Runtime.getRuntime().removeShutdownHook(cleanupHook);
            } catch (IllegalStateException e) {
                // JVM already shutting down; the hook performs the same cleanup
            }
        }
    }

    private static void execute(StoragePaths paths, String commandFile, InputStream in, PrintStream out) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new ParkController(paths), out);
        } catch (IOException e) {
            out.println("Error: could not load data from " + paths.getDirectory().toAbsolutePath() + ": "
                    + e.getMessage());
            return;
        }

        if (commandFile == null) {
            try {
                processor.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                out.println("Error: could not read standard input: " + e.getMessage());
            }
            return;
        }

        Path file = Path.of(commandFile);
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            out.println("Error: command file not found or not readable: " + file);
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            processor.run(reader);
        } catch (IOException e) {
            out.println("Error: could not read command file " + file + ": " + e.getMessage());
        }
    }

    private static Path createFlyDirectory(Path tmpRoot) throws IOException {
        String stamp = LocalDateTime.now().format(FLY_STAMP);
        Path dir = tmpRoot.resolve(stamp);
        for (int i = 1; Files.exists(dir); i++) {
            dir = tmpRoot.resolve(stamp + "_" + i);
        }
        return Files.createDirectories(dir);
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
                    // best effort: keep deleting the remaining files
                }
            });
        } catch (IOException e) {
            // directory vanished or unreadable; nothing more to do
        }
    }
}
