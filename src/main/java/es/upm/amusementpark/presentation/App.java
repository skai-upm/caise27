package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.service.ParkController;
import es.upm.amusementpark.business.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Application entry point. Reads commands from standard input, or from the
 * file given as the first argument, and processes them one per line. With
 * {@code --fly} as first argument all data is kept in a temporary directory
 * that is deleted at the end of the execution.
 */
public class App {

    static final Path DATA_DIR = Path.of("data");
    static final Path FLY_BASE_DIR = Path.of("tmp");
    static final String FLY = "--fly";

    public static void main(String[] args) {
        run(args, System.in, System.out, DATA_DIR, FLY_BASE_DIR);
    }

    static void run(String[] args, InputStream in, PrintStream out, Path dataDir) {
        run(args, in, out, dataDir, dataDir.resolveSibling("tmp"));
    }

    static void run(String[] args, InputStream in, PrintStream out, Path dataDir, Path flyBaseDir) {
        if (args.length > 0 && args[0].equals(FLY)) {
            Path flyDir;
            try {
                flyDir = ParkController.getInstance().createTemporaryDataDirectory(flyBaseDir);
            } catch (PersistenceException e) {
                out.println("ERROR: " + e.getMessage());
                return;
            }
            try {
                runWithData(Arrays.copyOfRange(args, 1, args.length), in, out, flyDir);
            } finally {
                try {
                    ParkController.getInstance().deleteTemporaryDataDirectory(flyDir, flyBaseDir);
                } catch (PersistenceException e) {
                    out.println("ERROR: " + e.getMessage());
                }
            }
            return;
        }
        runWithData(args, in, out, dataDir);
    }

    private static void runWithData(String[] args, InputStream in, PrintStream out, Path dataDir) {
        ParkController park = ParkController.getInstance();
        try {
            int skipped = park.load(dataDir, LocalDate.now());
            if (skipped > 0) {
                out.println("WARNING: " + skipped + " malformed line(s) in the data files were ignored.");
            }
        } catch (PersistenceException e) {
            out.println("ERROR: " + e.getMessage());
            return;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(out, new UserService(park.getUsers())));
        if (args.length == 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                runner.run(reader);
            } catch (IOException e) {
                out.println("ERROR: could not read standard input: " + e.getMessage());
            }
            return;
        }
        Path commandFile = Path.of(args[0]);
        if (!Files.exists(commandFile)) {
            out.println("ERROR: command file not found: " + args[0]);
            return;
        }
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            out.println("ERROR: command file cannot be read: " + args[0]);
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            out.println("ERROR: command file cannot be read: " + args[0]);
        }
    }
}
