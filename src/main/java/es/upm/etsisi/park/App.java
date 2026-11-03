package es.upm.etsisi.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Application entry point. Without arguments it reads commands from standard
 * input; with a file argument it reads them from that file. A leading
 * {@code --fly} argument keeps all data in a temporary directory that is removed
 * when the execution ends.
 */
public class App {

    static final String DATA_DIR_PROPERTY = "park.data.dir";
    static final String FLY = "--fly";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application and returns its exit status.
     */
    static int run(String[] args, InputStream in, PrintStream out) {
        Path dataDir = Path.of(System.getProperty(DATA_DIR_PROPERTY, "."));
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        String[] rest = fly ? Arrays.copyOfRange(args, 1, args.length) : args;

        FlyDirectory flyDirectory = null;
        if (fly) {
            try {
                flyDirectory = FlyDirectory.create(dataDir.resolve("tmp"), LocalDateTime.now());
            } catch (IOException e) {
                out.println("ERROR: cannot create temporary directory: " + e.getMessage());
                return 1;
            }
            dataDir = flyDirectory.getDirectory();
        }
        try {
            return execute(rest, dataDir, in, out);
        } finally {
            if (flyDirectory != null) {
                flyDirectory.delete(out);
            }
        }
    }

    private static int execute(String[] args, Path dataDir, InputStream in, PrintStream out) {
        Park park = Park.getInstance();
        try {
            park.start(new ParkStorage(dataDir), LocalDate.now());
        } catch (IOException e) {
            out.println("ERROR: cannot load park data from " + dataDir + ": " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(park, out));

        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                out.println("ERROR: cannot read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        Path commandFile = Path.of(args[0]);
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            out.println("ERROR: cannot read file " + commandFile);
            return 1;
        }
        return 0;
    }

    /** Timestamped temporary data directory used by --fly executions. */
    static final class FlyDirectory {

        private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

        private final Path root;
        private final boolean rootCreated;
        private final Path directory;

        private FlyDirectory(Path root, boolean rootCreated, Path directory) {
            this.root = root;
            this.rootCreated = rootCreated;
            this.directory = directory;
        }

        static FlyDirectory create(Path root, LocalDateTime now) throws IOException {
            boolean rootCreated = !Files.exists(root);
            Files.createDirectories(root);
            String stamp = now.format(STAMP);
            Path dir = root.resolve(stamp);
            for (int i = 1; Files.exists(dir); i++) {
                dir = root.resolve(stamp + "_" + i);
            }
            Files.createDirectory(dir);
            return new FlyDirectory(root, rootCreated, dir);
        }

        Path getDirectory() {
            return directory;
        }

        void delete(PrintStream out) {
            try {
                try (var paths = Files.walk(directory)) {
                    for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                        Files.delete(p);
                    }
                }
                if (rootCreated && isEmptyDirectory(root)) {
                    Files.delete(root);
                }
            } catch (IOException e) {
                out.println("ERROR: cannot delete temporary directory " + directory + ": " + e.getMessage());
            }
        }

        private static boolean isEmptyDirectory(Path dir) throws IOException {
            try (var entries = Files.list(dir)) {
                return entries.findAny().isEmpty();
            }
        }
    }
}
