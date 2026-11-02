package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.user.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.business.user.PasswordHasher;
import es.upm.etsisi.amusementpark.persistence.FileParkRepository;
import es.upm.etsisi.amusementpark.persistence.TemporaryDataDirectory;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Application entry point.
 *
 * <pre>
 * java -jar app.jar                  interactive
 * java -jar app.jar input.txt        command file
 * java -jar app.jar --fly            interactive, temporary persistence
 * java -jar app.jar --fly input.txt  command file, temporary persistence
 * </pre>
 */
public final class App {

    static final String FLY = "--fly";
    static final String TMP_DIR = "tmp";

    private App() {
    }

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, Path.of(""),
                new BCryptPasswordHasher(BCryptPasswordHasher.DEFAULT_LOG_ROUNDS));
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application with the given streams; normal data lives in {@code baseDir}.
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path baseDir,
                   PasswordHasher hasher) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        int first = fly ? 1 : 0;
        String commandFile = args.length > first ? args[first] : null;
        if (!fly) {
            return execute(commandFile, in, out, err, baseDir, hasher);
        }
        Path dataDir;
        try {
            dataDir = TemporaryDataDirectory.create(baseDir.resolve(TMP_DIR), LocalDateTime.now());
        } catch (IOException e) {
            err.println("ERROR: could not create temporary directory: " + e.getMessage());
            return 1;
        }
        try {
            return execute(commandFile, in, out, err, dataDir, hasher);
        } finally {
            try {
                TemporaryDataDirectory.delete(dataDir);
            } catch (IOException e) {
                err.println("ERROR: could not delete temporary directory " + dataDir + ": " + e.getMessage());
            }
        }
    }

    private static int execute(String commandFile, InputStream in, PrintStream out, PrintStream err, Path dataDir,
                               PasswordHasher hasher) {
        CommandProcessor processor;
        try {
            processor = ApplicationContext.start(new FileParkRepository(dataDir, err), LocalDate.now(), hasher);
        } catch (StorageException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(processor, out);
        if (commandFile == null) {
            return runInteractive(runner, in, err);
        }
        return runFile(runner, Path.of(commandFile), err);
    }

    private static int runInteractive(CommandRunner runner, InputStream in, PrintStream err) {
        try {
            runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            return 0;
        } catch (IOException e) {
            err.println("ERROR: could not read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandRunner runner, Path commands, PrintStream err) {
        if (!Files.isRegularFile(commands) || !Files.isReadable(commands)) {
            err.println("ERROR: command file not found or not readable: " + commands);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commands, StandardCharsets.UTF_8)) {
            runner.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: could not read command file " + commands + ": " + e.getMessage());
            return 1;
        }
    }
}
