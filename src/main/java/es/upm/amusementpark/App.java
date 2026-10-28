package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import es.upm.amusementpark.cli.CommandProcessor;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.persistence.DataLocation;

/**
 * Application entry point.
 *
 * <p>Accepted arguments: none (interactive), {@code <file>} (commands from a file), {@code --fly}
 * and {@code --fly <file>} (same, using a temporary data directory deleted at the end).
 */
public class App {

    static final Path DATA_DIRECTORY = Path.of(".");
    static final Path FLY_ROOT = Path.of("tmp");
    static final String FLY = "--fly";
    private static final DateTimeFormatter FLY_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final PrintStream out;
    private final CommandProcessor processor;

    App(PrintStream out, PrintStream err, DataLocation data) throws IOException {
        this.out = out;
        ParkController park = ParkController.getInstance();
        park.initialize(data).forEach(err::println);
        this.processor = new CommandProcessor(park);
    }

    public static void main(String[] args) {
        System.exit(execute(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, System.err, DATA_DIRECTORY, FLY_ROOT));
    }

    /**
     * Runs the application and returns the process exit status.
     */
    static int execute(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, Path dataDirectory,
            Path flyRoot) {
        if (args.length > 0 && args[0].equals(FLY)) {
            Path flyDirectory;
            try {
                flyDirectory = createFlyDirectory(flyRoot);
            } catch (IOException e) {
                err.println("Error: cannot create temporary directory in '" + flyRoot + "'");
                return 1;
            }
            try {
                return run(Arrays.copyOfRange(args, 1, args.length), stdin, out, err, flyDirectory);
            } finally {
                deleteRecursively(flyDirectory, err);
            }
        }
        return run(args, stdin, out, err, dataDirectory);
    }

    /**
     * Runs with permanent data in {@code dataDirectory}; temporary mode is not available here.
     */
    static int execute(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, Path dataDirectory) {
        return execute(args, stdin, out, err, dataDirectory, dataDirectory.resolve("tmp"));
    }

    private static int run(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, Path dataDirectory) {
        App app;
        try {
            app = new App(out, err, new DataLocation(dataDirectory));
        } catch (IOException e) {
            err.println("Error: cannot load data from '" + dataDirectory + "'");
            return 1;
        }
        if (args.length == 0) {
            return app.runQuietly(stdin, err);
        }
        Path file = Path.of(args[0]);
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            err.println("Error: cannot read file '" + args[0] + "'");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return app.runQuietly(reader, err);
        } catch (IOException e) {
            err.println("Error: cannot read file '" + args[0] + "'");
            return 1;
        }
    }

    static Path createFlyDirectory(Path flyRoot) throws IOException {
        String stamp = LocalDateTime.now().format(FLY_STAMP);
        Path directory = flyRoot.resolve(stamp);
        for (int i = 1; Files.exists(directory); i++) {
            directory = flyRoot.resolve(stamp + "_" + i);
        }
        return Files.createDirectories(directory);
    }

    private static void deleteRecursively(Path directory, PrintStream err) {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        } catch (IOException e) {
            err.println("Error: cannot delete temporary directory '" + directory + "'");
        }
    }

    private int runQuietly(BufferedReader reader, PrintStream err) {
        try {
            run(reader);
            return 0;
        } catch (IOException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Processes every line of the reader in order until the end of input or {@code EXIT}.
     */
    public void run(BufferedReader reader) throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            processLine(line);
        }
    }

    /**
     * Handles a single command line and prints its response.
     */
    void processLine(String line) {
        String response = processor.process(line);
        if (response != null) {
            out.println(response);
        }
    }
}
