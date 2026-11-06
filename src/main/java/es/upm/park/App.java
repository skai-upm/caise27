package es.upm.park;

import es.upm.park.command.CommandProcessor;
import es.upm.park.command.CommandRunner;
import es.upm.park.controller.ParkController;
import es.upm.park.persistence.StoragePaths;
import es.upm.park.persistence.TextParkStorage;

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
import java.util.stream.Stream;

/**
 * Application entry point. Supported forms: no arguments (interactive),
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
            TextParkStorage storage = new TextParkStorage(new StoragePaths(dataDir), err);
            park.restore(storage.load());
            park.setStorage(storage);
            CommandRunner runner = new CommandRunner(new CommandProcessor(), out);
            if (commandFile == null) {
                runReporting(runner, stdin, err);
            } else {
                runFile(runner, commandFile, err);
            }
        } finally {
            if (fly) {
                deleteRecursively(dataDir, err);
            }
        }
    }

    private static void runFile(CommandRunner runner, String commandFile, PrintStream err) {
        try (BufferedReader file = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            runner.run(file);
        } catch (IOException e) {
            err.println("ERROR: Cannot read command file: " + commandFile);
        }
    }

    private static void runReporting(CommandRunner runner, BufferedReader input, PrintStream err) {
        try {
            runner.run(input);
        } catch (IOException e) {
            err.println("ERROR: Cannot read input: " + e.getMessage());
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
