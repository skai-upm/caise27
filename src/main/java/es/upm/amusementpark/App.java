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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.CommandRunner;

/**
 * Application entry point. Supported argument forms:
 * <pre>
 * (none)          interactive
 * file            commands from file
 * --fly           interactive, temporary persistence
 * --fly file      commands from file, temporary persistence
 * </pre>
 */
public class App {

    static final String FLY = "--fly";
    private static final DateTimeFormatter TMP_NAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        System.exit(run(args, System.in, System.out, Path.of("")));
    }

    /**
     * Runs the application with {@code baseDir} as the normal data directory (and the parent of
     * {@code tmp/} in --fly mode).
     */
    static int run(String[] args, InputStream in, PrintStream out, Path baseDir) {
        return run(args, in, out, baseDir, new BCryptPasswordHasher());
    }

    static int run(String[] args, InputStream in, PrintStream out, Path baseDir, PasswordHasher hasher) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;

        Path tmpRoot = baseDir.resolve("tmp");
        boolean tmpRootCreated = fly && !Files.exists(tmpRoot);
        Path dataDir = baseDir;
        try {
            if (fly) {
                dataDir = createTemporaryDirectory(tmpRoot);
            }
            return runWith(dataDir, commandFile, in, out, hasher);
        } catch (IOException e) {
            out.println("ERROR: cannot create temporary directory: " + e.getMessage());
            return 1;
        } finally {
            if (fly) {
                deleteRecursively(dataDir, out);
                if (tmpRootCreated) {
                    deleteIfEmpty(tmpRoot);
                }
            }
        }
    }

    private static int runWith(Path dataDir, String commandFile, InputStream in, PrintStream out,
                               PasswordHasher hasher) {
        CommandProcessor processor;
        try {
            processor = AppInitializer.initialize(dataDir, hasher, out);
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(processor);
        if (commandFile == null) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                out.println("ERROR: cannot read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }
        Path commands = Path.of(commandFile);
        if (!Files.isRegularFile(commands) || !Files.isReadable(commands)) {
            out.println("ERROR: command file not found or not readable: " + commandFile);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commands, StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            out.println("ERROR: cannot read command file: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    private static Path createTemporaryDirectory(Path tmpRoot) throws IOException {
        String name = LocalDateTime.now(ZoneId.systemDefault()).format(TMP_NAME);
        Path dir = tmpRoot.resolve(name);
        for (int i = 1; Files.exists(dir); i++) {
            dir = tmpRoot.resolve(name + "_" + i);
        }
        return Files.createDirectories(dir);
    }

    private static void deleteRecursively(Path dir, PrintStream out) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        } catch (IOException e) {
            out.println("ERROR: cannot delete temporary directory " + dir + ": " + e.getMessage());
        }
    }

    private static void deleteIfEmpty(Path dir) {
        try (Stream<Path> entries = Files.list(dir)) {
            if (entries.findAny().isEmpty()) {
                Files.delete(dir);
            }
        } catch (IOException e) {
            // tmp/ left in place; it holds no data.
        }
    }
}
