package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.persistence.DataDirectory;
import es.upm.etsisi.amusementpark.persistence.PersistenceException;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.CommandRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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
                flyDir = DataDirectory.createTemporary(workDir.resolve(TMP_DIR));
                dataDir = flyDir;
            }
            CommandRunner runner = new CommandRunner(ApplicationInitializer.initialize(dataDir, out));
            return rest.length == 0 ? runInteractive(runner, in, out) : runFile(runner, Path.of(rest[0]), out);
        } catch (IOException | PersistenceException e) {
            out.println("ERROR: " + e.getMessage());
            return 1;
        } finally {
            if (flyDir != null) {
                deleteQuietly(flyDir, out);
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

    private static void deleteQuietly(Path dir, PrintStream out) {
        try {
            DataDirectory.deleteTemporary(dir);
        } catch (PersistenceException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }
}
