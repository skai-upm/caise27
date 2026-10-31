package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.persistence.TemporaryStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * Application entry point. Without a file argument commands are read from
 * standard input; otherwise from the given command file. A leading
 * {@code --fly} argument stores every file of the run in a temporary
 * directory that is deleted on termination. All modes feed the same
 * {@link CommandProcessor}.
 */
public class App {

    static final String FLY = "--fly";
    static final Path DEFAULT_DATA_DIR = Path.of(".");

    public static void main(String[] args) {
        run(args, System.in, System.out, System.err, DEFAULT_DATA_DIR);
    }

    /**
     * Runs the application with its permanent data in {@code dataDir} (or in
     * {@code dataDir/tmp/<timestamp>} with {@code --fly}) and returns its exit
     * status: 0 on normal termination, 1 if a file could not be read or written.
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path dataDir) {
        boolean fly = args.length > 0 && args[0].equals(FLY);
        String[] commandArgs = fly ? Arrays.copyOfRange(args, 1, args.length) : args;

        Path storage = dataDir;
        if (fly) {
            try {
                storage = TemporaryStorage.create(dataDir.resolve("tmp"));
            } catch (IOException e) {
                err.println("ERROR: cannot create temporary directory: " + e.getMessage());
                return 1;
            }
        }
        try {
            return execute(commandArgs, in, out, err, new StoragePaths(storage));
        } finally {
            if (fly) {
                removeTemporaryStorage(storage, err);
            }
        }
    }

    private static int execute(String[] args, InputStream in, PrintStream out, PrintStream err,
                               StoragePaths paths) {
        ParkController park;
        try {
            park = ApplicationContext.start(paths, LocalDate.now(ZoneId.systemDefault()));
        } catch (IOException e) {
            err.println("ERROR: cannot load data from " + paths.directory() + ": " + e.getMessage());
            return 1;
        }
        try {
            return readCommands(args, in, err, new CommandProcessor(park, out));
        } finally {
            ApplicationContext.stop();
        }
    }

    private static int readCommands(String[] args, InputStream in, PrintStream err, CommandProcessor processor) {
        if (args.length == 0) {
            return execute(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)),
                    processor, err, "standard input");
        }

        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            err.println("ERROR: cannot read file " + commandFile + ": file does not exist or is not readable");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            return execute(reader, processor, err, commandFile.toString());
        } catch (IOException e) {
            err.println("ERROR: cannot read file " + commandFile + ": " + e.getMessage());
            return 1;
        }
    }

    private static int execute(BufferedReader reader, CommandProcessor processor,
                               PrintStream err, String source) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!processor.process(line)) {
                    break;
                }
            }
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read " + source + ": " + e.getMessage());
            return 1;
        }
    }

    private static void removeTemporaryStorage(Path directory, PrintStream err) {
        try {
            TemporaryStorage.delete(directory);
        } catch (IOException e) {
            err.println("WARNING: could not delete temporary directory " + directory + ": " + e.getMessage());
        }
    }
}
