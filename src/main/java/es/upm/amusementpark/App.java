package es.upm.amusementpark;

import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.CommandRunner;
import es.upm.amusementpark.presentation.ExtendedHelpFormatter;
import es.upm.amusementpark.presentation.HelpGenerator;
import es.upm.amusementpark.presentation.SyntaxHelpFormatter;
import es.upm.amusementpark.persistence.TemporaryDataDirectory;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Entry point of the amusement park application. Without arguments it reads
 * commands from standard input; with an argument it reads them from that file.
 * A leading {@code --fly} argument keeps all data in a temporary directory
 * that is deleted when the run ends.
 */
public final class App {

    static final String FLY = "--fly";
    static final String TEMPORARY_ROOT = "tmp";

    private App() {
    }

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, Path.of(""));
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application with its data in {@code workDir} (or in a temporary
     * directory under it with {@code --fly}) and returns the exit status.
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path workDir) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        int fileArgument = fly ? 1 : 0;
        String commandFile = args.length > fileArgument ? args[fileArgument] : null;
        if (!fly) {
            return runWith(workDir, commandFile, in, out, err);
        }

        TemporaryDataDirectory temporary;
        try {
            temporary = TemporaryDataDirectory.create(workDir.resolve(TEMPORARY_ROOT),
                    LocalDateTime.now(ZoneId.systemDefault()));
        } catch (IOException e) {
            out.println("ERROR: Cannot create the temporary data directory: " + e.getMessage());
            return 1;
        }
        try (temporary) {
            return runWith(temporary.getDirectory(), commandFile, in, out, err);
        } catch (IOException e) {
            out.println("ERROR: Cannot delete the temporary data directory " + temporary.getDirectory());
            return 1;
        }
    }

    private static int runWith(Path dataDir, String commandFile, InputStream in, PrintStream out,
                               PrintStream err) {
        Park park;
        try {
            park = ApplicationContext.start(ApplicationContext.fileRepositories(dataDir, err),
                    LocalDate.now(ZoneId.systemDefault()), new BCryptPasswordHasher());
        } catch (IOException e) {
            out.println("ERROR: Cannot load park data from " + dataDir.toAbsolutePath() + ": " + e.getMessage());
            return 1;
        }
        CommandProcessor processor = new CommandProcessor(park, new HelpGenerator(new SyntaxHelpFormatter()),
                new HelpGenerator(new ExtendedHelpFormatter()));
        CommandRunner runner = new CommandRunner(processor, out);

        if (commandFile == null) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                return 0;
            } catch (IOException e) {
                out.println("ERROR: Cannot read standard input: " + e.getMessage());
                return 1;
            }
        }

        try (BufferedReader reader = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            runner.run(reader);
            return 0;
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR: Cannot read command file '" + commandFile + "'");
            return 1;
        }
    }
}
