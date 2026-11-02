package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import es.upm.etsisi.amusementpark.command.CommandProcessor;
import es.upm.etsisi.amusementpark.command.CommandRunner;
import es.upm.etsisi.amusementpark.park.DataPaths;
import es.upm.etsisi.amusementpark.park.Park;
import es.upm.etsisi.amusementpark.persistence.TemporaryDirectory;

/**
 * Application entry point. Supported forms: no arguments (interactive), a command
 * file path, {@code --fly}, or {@code --fly} followed by a command file path.
 * With {@code --fly} all data lives in a temporary directory deleted at the end.
 */
public class App {
    static final String FLY = "--fly";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, Path.of(""));
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application with data stored under {@code workDir} (or {@code workDir/tmp/...} with --fly).
     */
    static int run(String[] args, InputStream in, PrintStream out, Path workDir) {
        boolean fly = args.length > 0 && args[0].equals(FLY);
        int fileArgument = fly ? 1 : 0;
        String commandFile = args.length > fileArgument ? args[fileArgument] : null;
        Path flyDir = null;
        try {
            Path dataDir = workDir;
            if (fly) {
                flyDir = TemporaryDirectory.create(workDir.resolve("tmp"));
                dataDir = flyDir;
            }
            return execute(commandFile, in, out, dataDir);
        } catch (IOException e) {
            out.println("ERROR: cannot prepare data directory: " + e.getMessage());
            return 1;
        } finally {
            if (flyDir != null) {
                try {
                    TemporaryDirectory.delete(flyDir);
                } catch (IOException e) {
                    out.println("ERROR: cannot delete temporary directory " + flyDir + ": " + e.getMessage());
                }
            }
        }
    }

    private static int execute(String commandFile, InputStream in, PrintStream out, Path dataDir) {
        Park park = Park.getInstance();
        try {
            park.load(DataPaths.in(dataDir));
        } catch (IOException e) {
            out.println("ERROR: cannot load park data from " + dataDir.toAbsolutePath() + ": " + e.getMessage());
            return 1;
        }
        for (String warning : park.getLoadWarnings()) {
            out.println("WARNING: " + warning);
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(park), out);
        if (commandFile != null) {
            return runner.runFile(Path.of(commandFile)) ? 0 : 1;
        }
        try {
            runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            return 0;
        } catch (IOException e) {
            out.println("ERROR: cannot read standard input: " + e.getMessage());
            return 1;
        }
    }
}
