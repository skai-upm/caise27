package es.upm.etsisi.park.presentation;

import es.upm.etsisi.park.business.controller.ParkController;
import es.upm.etsisi.park.business.security.BCryptPasswordHasher;
import es.upm.etsisi.park.business.security.PasswordHasher;
import es.upm.etsisi.park.persistence.ParkStorage;
import es.upm.etsisi.park.persistence.PersistenceException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Application entry point.
 * Usage: {@code java -jar amusement-park.jar [--fly] [commandFile]}.
 * Without a command file, commands are read from standard input.
 */
public final class App {

    static final String DATA_DIR_PROPERTY = "park.data.dir";
    static final String FLY_OPTION = "--fly";

    private App() {
    }

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, new BCryptPasswordHasher());
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, PasswordHasher hasher) {
        boolean fly = args.length > 0 && FLY_OPTION.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;
        Path baseDir = Path.of(System.getProperty(DATA_DIR_PROPERTY, "."));

        TemporaryDirectory temporary = null;
        try {
            Path dataDir = baseDir;
            if (fly) {
                temporary = TemporaryDirectory.create(baseDir);
                dataDir = temporary.path();
            }
            return execute(ParkStorage.inDirectory(dataDir, err), hasher, commandFile, in, out, err);
        } catch (IOException e) {
            err.println("ERROR: Cannot prepare temporary directory: " + e.getMessage());
            return 1;
        } finally {
            if (temporary != null) {
                temporary.delete(err);
            }
        }
    }

    private static int execute(ParkStorage storage, PasswordHasher hasher, String commandFile, InputStream in,
                               PrintStream out, PrintStream err) {
        ParkController park;
        try {
            park = ParkInitializer.initialize(storage, hasher, LocalDate.now());
        } catch (PersistenceException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(ParkInitializer.commandProcessor(park), out);

        if (commandFile == null) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), true);
            } catch (IOException e) {
                err.println("ERROR: Cannot read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        Path path = Path.of(commandFile);
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            err.println("ERROR: Command file '" + commandFile + "' does not exist or cannot be read");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            runner.run(reader, false);
        } catch (IOException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
