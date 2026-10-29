package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.StorageConfiguration;
import es.upm.amusementpark.presentation.CommandRunner;

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

/**
 * Application entry point. Supported invocations:
 * <pre>
 * java -jar app.jar                     interactive, persistent data
 * java -jar app.jar input.txt           command file, persistent data
 * java -jar app.jar --fly               interactive, temporary data
 * java -jar app.jar --fly input.txt     command file, temporary data
 * </pre>
 */
public class App {

    static final String FLY = "--fly";

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        int status = run(args, System.in, System.out, System.err, Path.of(""), now,
                new BCryptPasswordHasher(BCryptPasswordHasher.DEFAULT_LOG_ROUNDS));
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application with the given streams, working directory, start time and password hasher.
     *
     * @return the process exit status
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path workDir,
                   LocalDateTime now, PasswordHasher hasher) {
        boolean fly = args.length > 0 && args[0].equals(FLY);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;

        StorageConfiguration storage;
        try {
            storage = fly ? StorageConfiguration.temporary(workDir, now, err)
                    : StorageConfiguration.persistent(workDir, err);
        } catch (IOException e) {
            err.println("ERROR: could not create temporary directory: " + e.getMessage());
            return 1;
        }
        try {
            return execute(storage, commandFile, in, out, err, now.toLocalDate(), hasher);
        } finally {
            try {
                storage.cleanup();
            } catch (IOException e) {
                err.println("ERROR: could not delete temporary directory: " + e.getMessage());
            }
        }
    }

    private static int execute(StorageConfiguration storage, String commandFile, InputStream in,
                               PrintStream out, PrintStream err, LocalDate today, PasswordHasher hasher) {
        Park park;
        try {
            park = ParkInitializer.initialize(storage.createRepositories(), hasher, today);
        } catch (PersistenceException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(ParkInitializer.commandProcessor(park, out));

        if (commandFile == null) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                err.println("ERROR: could not read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        Path file = Path.of(commandFile);
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            err.println("ERROR: command file not found or not readable: " + commandFile);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            err.println("ERROR: could not read command file: " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
