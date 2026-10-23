package es.upm.park;

import es.upm.park.cli.CommandProcessor;
import es.upm.park.cli.CommandRunner;
import es.upm.park.persistence.DataPaths;
import es.upm.park.service.Park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Application entry point. Accepted forms:
 * <pre>
 * java -jar app.jar                 interactive, permanent data
 * java -jar app.jar input.txt       command file, permanent data
 * java -jar app.jar --fly           interactive, temporary data
 * java -jar app.jar --fly input.txt command file, temporary data
 * </pre>
 */
public final class App {

    static final String DATA_DIR_PROPERTY = "park.data.dir";
    static final String DEFAULT_DATA_DIR = ".";
    static final String FLY_OPTION = "--fly";
    static final String TMP_ROOT_PROPERTY = "park.tmp.root";
    static final String DEFAULT_TMP_ROOT = "tmp";
    private static final DateTimeFormatter FLY_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private App() {
    }

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        boolean fly = args.length > 0 && FLY_OPTION.equals(args[0]);
        int fileArgument = fly ? 1 : 0;
        Path commandFile = args.length > fileArgument ? Path.of(args[fileArgument]) : null;
        if (commandFile != null && (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile))) {
            err.println("ERROR: command file not found or not readable: " + commandFile);
            return 1;
        }

        Path tmpRoot = Path.of(System.getProperty(TMP_ROOT_PROPERTY, DEFAULT_TMP_ROOT));
        boolean tmpRootExisted = Files.isDirectory(tmpRoot);
        Path dataDir;
        try {
            dataDir = fly ? createFlyDirectory(tmpRoot)
                    : Path.of(System.getProperty(DATA_DIR_PROPERTY, DEFAULT_DATA_DIR));
        } catch (IOException e) {
            err.println("ERROR: cannot create temporary data directory: " + e.getMessage());
            return 1;
        }

        try {
            return execute(dataDir, commandFile, in, out, err);
        } finally {
            if (fly) {
                deleteRecursively(dataDir, err);
                if (!tmpRootExisted) {
                    deleteIfEmpty(tmpRoot);
                }
            }
        }
    }

    private static int execute(Path dataDir, Path commandFile, InputStream in, PrintStream out, PrintStream err) {
        Park park = Park.getInstance();
        try {
            park.open(new DataPaths(dataDir));
        } catch (UncheckedIOException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(park), out);

        if (commandFile == null) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                return 0;
            } catch (IOException e) {
                err.println("ERROR: cannot read standard input: " + e.getMessage());
                return 1;
            }
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read command file " + commandFile + ": " + e.getMessage());
            return 1;
        }
    }

    /**
     * Creates {@code <tmpRoot>/yyyyMMdd_HHmmss}, adding a numeric suffix if
     * a directory with that timestamp already exists.
     */
    static Path createFlyDirectory(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String stamp = LocalDateTime.now().format(FLY_DIR_FORMAT);
        Path candidate = tmpRoot.resolve(stamp);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = tmpRoot.resolve(stamp + "_" + suffix++);
        }
        return Files.createDirectory(candidate);
    }

    private static void deleteRecursively(Path directory, PrintStream err) {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            err.println("WARNING: could not delete temporary directory " + directory + ": " + e.getMessage());
        }
    }

    private static void deleteIfEmpty(Path directory) {
        try (Stream<Path> entries = Files.list(directory)) {
            if (entries.findAny().isEmpty()) {
                Files.delete(directory);
            }
        } catch (IOException e) {
            // Leaving an empty tmp/ directory behind is harmless.
        }
    }
}
