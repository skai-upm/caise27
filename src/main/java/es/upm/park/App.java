package es.upm.park;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.exception.ParkException;
import es.upm.park.presentation.CommandRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Application entry point and composition root: parses the arguments, configures
 * the persistence layer, restores the park state and hands control to the
 * presentation layer. Supported forms: no arguments (interactive),
 * {@code <file>}, {@code --fly} and {@code --fly <file>}. Normal runs keep data in
 * {@code data/}; {@code --fly} runs use a fresh {@code tmp/<timestamp>/} directory
 * that is deleted when the execution ends.
 */
public class App {

    static final String FLY = "--fly";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        run(args, Path.of("."), new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, System.err);
    }

    static void run(String[] args, Path baseDir, BufferedReader stdin, PrintStream out, PrintStream err) {
        run(args, baseDir, stdin, out, err, configured -> { });
    }

    /** As {@link #run}, letting the caller adjust the configured objects before commands run (tests). */
    static void run(String[] args, Path baseDir, BufferedReader stdin, PrintStream out, PrintStream err,
                    Consumer<AppConfiguration.Configured> customizer) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        String commandFile = fly ? (args.length > 1 ? args[1] : null) : (args.length > 0 ? args[0] : null);
        Path dataDir;
        try {
            dataDir = fly ? createTemporaryDirectory(baseDir.resolve("tmp")) : baseDir.resolve("data");
        } catch (IOException e) {
            err.println("ERROR: Cannot create the temporary data directory: " + e.getMessage());
            return;
        }
        try {
            ParkController park = ParkController.getInstance();
            AppConfiguration.Configured configured = AppConfiguration.configure(dataDir, err);
            try {
                configured.users().restore(configured.userStorage().load());
                park.restore(configured.storage().load());
            } catch (ParkException e) {
                err.println("ERROR: " + e.getMessage());
                return;
            }
            customizer.accept(configured);
            CommandRunner runner = new CommandRunner(configured.processor(), out);
            if (commandFile == null) {
                runner.runInteractive(stdin, err);
            } else {
                runner.runFile(commandFile, err);
            }
        } finally {
            if (fly) {
                deleteRecursively(dataDir, err);
            }
        }
    }

    static Path createTemporaryDirectory(Path tmpRoot) throws IOException {
        String stamp = LocalDateTime.now().format(STAMP);
        Path dir = tmpRoot.resolve(stamp);
        for (int n = 1; Files.exists(dir); n++) {
            dir = tmpRoot.resolve(stamp + "_" + n);
        }
        return Files.createDirectories(dir);
    }

    private static void deleteRecursively(Path dir, PrintStream err) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        } catch (IOException e) {
            err.println("ERROR: Cannot delete temporary directory " + dir + ": " + e.getMessage());
        }
    }
}
