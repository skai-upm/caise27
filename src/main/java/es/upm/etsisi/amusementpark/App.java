package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Application entry point. Supported forms: no arguments (interactive), {@code <file>},
 * {@code --fly} and {@code --fly <file>}. With --fly all data lives in a temporary directory
 * that is deleted when execution ends.
 */
public class App {

    static final String FLY = "--fly";
    private static final DateTimeFormatter FLY_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        System.exit(run(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, System.err, Path.of("")));
    }

    /**
     * Runs the application with data stored under {@code workDir} and returns the process exit status.
     */
    static int run(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, Path workDir) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        int fileArg = fly ? 1 : 0;
        String commandFile = args.length > fileArg ? args[fileArg] : null;
        Path flyDir = null;
        try {
            Path dataDir = workDir;
            if (fly) {
                flyDir = createFlyDirectory(workDir.resolve("tmp"));
                dataDir = flyDir;
            }
            ParkController park = ParkController.getInstance();
            park.load(new Storage(dataDir), LocalDate.now());
            CommandProcessor processor = new CommandProcessor(park);
            if (commandFile == null) {
                return execute(processor, stdin, out, err);
            }
            Path file = Path.of(commandFile);
            if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
                err.println("ERROR: command file not found or not readable: " + file);
                return 1;
            }
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                return execute(processor, reader, out, err);
            }
        } catch (IOException | RuntimeException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        } finally {
            if (flyDir != null) {
                deleteFlyDirectory(flyDir, err);
            }
        }
    }

    private static int execute(CommandProcessor processor, BufferedReader input, PrintStream out, PrintStream err) {
        try {
            processor.run(input, out);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read commands: " + e.getMessage());
            return 1;
        }
    }

    static Path createFlyDirectory(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String stamp = LocalDateTime.now().format(FLY_DIR_FORMAT);
        Path dir = tmpRoot.resolve(stamp);
        for (int n = 1; Files.exists(dir); n++) {
            dir = tmpRoot.resolve(stamp + "_" + n);
        }
        return Files.createDirectory(dir);
    }

    private static void deleteFlyDirectory(Path dir, PrintStream err) {
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
            Path parent = dir.getParent();
            try (Stream<Path> rest = Files.list(parent)) {
                if (rest.findAny().isEmpty()) {
                    Files.delete(parent);
                }
            }
        } catch (IOException e) {
            err.println("WARNING: could not delete temporary directory " + dir + ": " + e.getMessage());
        }
    }
}
