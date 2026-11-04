package com.parque;

import com.parque.persistence.DataDirectory;
import com.parque.presentation.CommandProcessor;
import com.parque.presentation.CommandRunner;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap (composition root). Interprets the launch arguments — optional {@code --fly} and an optional command
 * file — chooses the data directory, wires the business controller and the presentation layer, and returns the
 * exit status. Without a command file commands are read from standard input.
 */
public class App {
    /** Data files live in the working directory. */
    static final Path DATA_DIR = Path.of("");
    static final String FLY_OPTION = "--fly";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, DATA_DIR, true);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application and returns the process exit status (0 normal end, 1 controlled failure).
     */
    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path baseDir) {
        return run(args, stdin, out, err, baseDir, false);
    }

    private static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path baseDir,
            boolean registerShutdownHook) {
        boolean fly = false;
        List<String> rest = new ArrayList<>();
        for (String a : args) {
            if (a.equals(FLY_OPTION)) {
                fly = true;
            } else {
                rest.add(a);
            }
        }
        String commandFile = rest.isEmpty() ? null : rest.get(0);
        if (!fly) {
            return runWithDataDir(commandFile, stdin, out, err, baseDir);
        }
        Path flyDir;
        try {
            flyDir = DataDirectory.createTemporary(baseDir, LocalDateTime.now());
        } catch (IOException e) {
            err.println("ERROR: Cannot create temporary directory for --fly: " + CommandRunner.describe(e));
            return 1;
        }
        Thread cleanup = new Thread(() -> deleteQuietly(flyDir, err));
        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(cleanup);
        }
        try {
            return runWithDataDir(commandFile, stdin, out, err, flyDir);
        } finally {
            deleteQuietly(flyDir, err);
            if (registerShutdownHook) {
                try {
                    Runtime.getRuntime().removeShutdownHook(cleanup);
                } catch (IllegalStateException alreadyShuttingDown) {
                    // the hook is running or will run; deletion is idempotent
                }
            }
        }
    }

    private static int runWithDataDir(String commandFile, InputStream stdin, PrintStream out, PrintStream err,
            Path dataDir) {
        try {
            ApplicationContext.start(dataDir);
        } catch (IOException e) {
            ApplicationContext.stop();
            err.println("ERROR: Cannot load data from '" + dataDir.toAbsolutePath() + "': " + CommandRunner.describe(e));
            return 1;
        }
        try {
            CommandRunner runner = new CommandRunner(new CommandProcessor(), out, err);
            return commandFile == null ? runner.runInteractive(stdin) : runner.runFile(commandFile);
        } finally {
            ApplicationContext.stop();
        }
    }

    private static void deleteQuietly(Path dir, PrintStream err) {
        try {
            DataDirectory.delete(dir);
        } catch (IOException e) {
            err.println("ERROR: Cannot delete temporary directory '" + dir + "': " + CommandRunner.describe(e));
        }
    }
}
