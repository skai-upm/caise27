package es.upm.park.presentation;

import es.upm.park.business.ApplicationContext;
import es.upm.park.business.ParkController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Entry point. Without a command-file argument commands are read from standard input;
 * otherwise the first argument other than {@code --fly} is the path of a command file. Both
 * sources share the same processing. With {@code --fly} the business layer works on a
 * temporary, per-run data directory that is deleted when the run ends.
 */
public class App {

    /** Data files live in the working directory. */
    static final Path DATA_DIR = Path.of("");
    /** Root of the temporary run directories used by {@code --fly}. */
    static final Path TMP_ROOT = Path.of("tmp");
    static final String FLY_FLAG = "--fly";

    /** Application context of the current run, closed on abnormal termination. */
    private static final AtomicReference<ApplicationContext> ACTIVE = new AtomicReference<>();

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ApplicationContext context = ACTIVE.getAndSet(null);
            if (context != null) {
                context.close();
            }
        }));
        int status = run(args, System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Runs the application and returns its exit status (0 = normal end). */
    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err) {
        return run(args, stdin, out, err, DATA_DIR, TMP_ROOT);
    }

    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path dataDir) {
        return run(args, stdin, out, err, dataDir, TMP_ROOT);
    }

    /** Runs with the persistent data in {@code dataDir}, or in a new directory under {@code tmpRoot} with --fly. */
    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path dataDir, Path tmpRoot) {
        List<String> commandArgs = new ArrayList<>(List.of(args));
        boolean fly = commandArgs.removeIf(FLY_FLAG::equals);
        String[] rest = commandArgs.toArray(String[]::new);
        ApplicationContext context;
        try {
            context = fly ? ApplicationContext.startTemporary(tmpRoot) : ApplicationContext.start(dataDir);
        } catch (IOException e) {
            err.println(fly
                    ? "ERROR: Cannot create temporary directory under '" + tmpRoot + "': " + describe(e)
                    : "ERROR: Cannot load data files: " + describe(e));
            return 1;
        }
        ACTIVE.set(context);
        context.getTemporaryDirectory()
                .ifPresent(dir -> err.println("INFO: --fly mode, temporary data in " + dir));
        try (context) {
            return execute(rest, stdin, out, err, new CommandRunner(createProcessor(context.getController())));
        } finally {
            ACTIVE.compareAndSet(context, null);
        }
    }

    /** Presentation wiring: the command processor with its help generators. */
    static CommandProcessor createProcessor(ParkController controller) {
        CommandCatalog catalog = CommandCatalog.standard();
        return new CommandProcessor(controller,
                new HelpGenerator(catalog, new SyntaxHelpFormatter()),
                new HelpGenerator(catalog, new ExtendedHelpFormatter()));
    }

    private static int execute(String[] args, InputStream stdin, PrintStream out, PrintStream err, CommandRunner runner) {
        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            } catch (IOException e) {
                err.println("ERROR: Cannot read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }
        String commandFile = args[0];
        Path path;
        try {
            path = Path.of(commandFile);
        } catch (InvalidPathException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + describe(e));
            return 1;
        }
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            runner.run(in, out);
        } catch (IOException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + describe(e));
            return 1;
        }
        return 0;
    }

    private static String describe(Exception e) {
        String reason = e.getMessage();
        return e.getClass().getSimpleName() + (reason == null ? "" : " (" + reason + ")");
    }
}
