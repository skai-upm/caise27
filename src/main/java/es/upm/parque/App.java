package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Entry point. Without a file argument it reads commands from standard input;
 * with one it reads them from that file. The {@code --fly} option keeps all persistence
 * in a temporary directory that is deleted when the run ends.
 */
public class App {

    static final Path DATA_DIR = Path.of("data");
    static final Path TMP_ROOT = Path.of("tmp");
    static final String FLY_OPTION = "--fly";
    private static final String USAGE = "Usage: java -jar parque.jar [--fly] [commandFile]";
    private static final DateTimeFormatter TMP_NAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    record Arguments(boolean fly, String file) {

        static Arguments parse(String[] args) {
            boolean fly = false;
            String file = null;
            for (String arg : args) {
                if (FLY_OPTION.equals(arg)) {
                    fly = true;
                } else if (arg.startsWith("--")) {
                    throw new IllegalArgumentException("Unknown option: " + arg);
                } else if (file != null) {
                    throw new IllegalArgumentException("Only one command file can be given");
                } else {
                    file = arg;
                }
            }
            return new Arguments(fly, file);
        }
    }

    public static void main(String[] args) {
        run(args, DATA_DIR, TMP_ROOT, System.in, System.out);
    }

    static void run(String[] args, Path dataDir, Path tmpRoot, InputStream in, PrintStream out) {
        Arguments arguments;
        try {
            arguments = Arguments.parse(args);
        } catch (IllegalArgumentException e) {
            out.println("ERROR " + e.getMessage());
            out.println(USAGE);
            return;
        }
        Path tempDir = null;
        Thread cleanupHook = null;
        try {
            Path dir = dataDir;
            if (arguments.fly()) {
                tempDir = createTempDataDir(tmpRoot);
                cleanupHook = registerCleanup(tempDir);
                dir = tempDir;
            }
            execute(arguments.file(), new ParkController(dir), in, out);
        } catch (IOException | RuntimeException e) {
            out.println("ERROR Cannot start the application: " + e.getMessage());
        } finally {
            if (tempDir != null) {
                deleteRecursively(tempDir);
                unregister(cleanupHook);
            }
        }
    }

    private static void execute(String file, ParkController park, InputStream in, PrintStream out) {
        CommandProcessor processor = new CommandProcessor(park, out);
        if (file == null) {
            try {
                processCommands(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), processor);
            } catch (IOException e) {
                out.println("ERROR Cannot read standard input: " + e.getMessage());
            }
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
            processCommands(reader, processor);
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR Cannot read command file '" + file + "': " + describe(e));
        }
    }

    static void processCommands(BufferedReader reader, CommandProcessor processor) throws IOException {
        String line;
        boolean running = true;
        while (running && (line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                running = processor.process(line);
            }
        }
    }

    static Path createTempDataDir(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String base = LocalDateTime.now(ZoneId.systemDefault()).format(TMP_NAME);
        Path dir = tmpRoot.resolve(base);
        for (int suffix = 1; Files.exists(dir); suffix++) {
            dir = tmpRoot.resolve(base + "_" + suffix);
        }
        return Files.createDirectory(dir);
    }

    static void deleteRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot delete temporary directory " + dir, e);
        }
    }

    private static Thread registerCleanup(Path tempDir) {
        Thread hook = new Thread(() -> deleteRecursively(tempDir));
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    private static void unregister(Thread hook) {
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException e) {
            // JVM already shutting down: the hook itself performs the cleanup
        }
    }

    private static String describe(Exception e) {
        if (e instanceof NoSuchFileException) {
            return "file not found";
        }
        if (e instanceof AccessDeniedException) {
            return "access denied";
        }
        return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : " " + e.getMessage());
    }
}
