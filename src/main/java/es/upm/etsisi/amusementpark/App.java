package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.cli.CommandProcessor;
import es.upm.etsisi.amusementpark.cli.CommandRunner;
import es.upm.etsisi.amusementpark.controller.ParkController;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Application entry point.
 * <ul>
 *     <li>no arguments: interactive mode;</li>
 *     <li>{@code <file>}: commands from a file;</li>
 *     <li>{@code --fly [<file>]}: same, with data kept in a temporary directory deleted at the end.</li>
 * </ul>
 */
public class App {

    static final String FLY = "--fly";
    static final String TMP_DIR = "tmp";
    private static final DateTimeFormatter FLY_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        System.exit(run(args, Path.of(""), System.in, System.out));
    }

    static int run(String[] args, Path workDir, InputStream in, PrintStream out) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        String[] rest = fly ? Arrays.copyOfRange(args, 1, args.length) : args;
        Path flyDir = null;
        try {
            Path dataDir = workDir;
            if (fly) {
                flyDir = createFlyDirectory(workDir.resolve(TMP_DIR));
                dataDir = flyDir;
            }
            ParkController park = ParkController.getInstance();
            park.initialize(dataDir);
            CommandRunner runner = new CommandRunner(new CommandProcessor(park, out));
            return rest.length == 0 ? runInteractive(runner, in, out) : runFile(runner, Path.of(rest[0]), out);
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
            return 1;
        } finally {
            if (flyDir != null) {
                deleteFlyDirectory(flyDir, out);
            }
        }
    }

    private static int runInteractive(CommandRunner runner, InputStream in, PrintStream out) throws IOException {
        out.println("Amusement Park - type HELP for the list of commands");
        runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
        return 0;
    }

    private static int runFile(CommandRunner runner, Path commandFile, PrintStream out) throws IOException {
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            out.println("ERROR: Command file not found or not readable: " + commandFile);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader);
        }
        return 0;
    }

    static Path createFlyDirectory(Path tmpRoot) throws IOException {
        String stamp = LocalDateTime.now().format(FLY_DIR_FORMAT);
        Path dir = tmpRoot.resolve(stamp);
        for (int n = 1; Files.exists(dir); n++) {
            dir = tmpRoot.resolve(stamp + "_" + n);
        }
        return Files.createDirectories(dir);
    }

    private static void deleteFlyDirectory(Path dir, PrintStream out) {
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
            Path parent = dir.getParent();
            try (Stream<Path> siblings = Files.list(parent)) {
                if (siblings.findAny().isEmpty()) {
                    Files.delete(parent);
                }
            }
        } catch (IOException e) {
            out.println("ERROR: Could not delete temporary directory " + dir + ": " + e.getMessage());
        }
    }
}
