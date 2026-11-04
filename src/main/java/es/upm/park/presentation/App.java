package es.upm.park.presentation;

import es.upm.park.business.service.ParkController;
import es.upm.park.business.service.ParkSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Application entry point. Without a command-file argument commands are read from
 * standard input; otherwise the first argument is the path of a command file. Both
 * modes share the same {@link CommandRunner}. The {@code --fly} option runs on a
 * temporary per-run data directory that is deleted at the end.
 */
public class App {

    static final String FLY_OPTION = "--fly";
    static final String TMP_DIR = "tmp";

    public static void main(String[] args) {
        int status = launch(args, Path.of(""), LocalDateTime.now(), System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Interprets the launch arguments: {@code --fly} selects temporary data under
     * {@code workDir/tmp}, otherwise the permanent files of {@code workDir} are used;
     * the remaining arguments select interactive or file mode.
     */
    static int launch(String[] args, Path workDir, LocalDateTime start, InputStream stdin, PrintStream out,
                      PrintStream err) {
        List<String> rest = new ArrayList<>();
        boolean fly = false;
        for (String a : args) {
            if (FLY_OPTION.equals(a)) {
                fly = true;
            } else {
                rest.add(a);
            }
        }
        ParkSession session;
        try {
            session = fly ? ParkSession.temporary(workDir.resolve(TMP_DIR), start) : ParkSession.permanent(workDir);
        } catch (UncheckedIOException e) {
            err.println("ERROR: cannot load data: " + e.getMessage());
            return 1;
        }
        try (session) {
            return run(rest.toArray(new String[0]), session.controller(), stdin, out, err);
        } catch (UncheckedIOException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
    }

    /** Runs the commands against {@code park} and returns the exit status (0 on success). */
    static int run(String[] args, ParkController park, InputStream stdin, PrintStream out, PrintStream err) {
        CommandRunner runner = new CommandRunner(newCommandProcessor(park));
        if (args.length == 0) {
            return runInteractive(runner, stdin, out, err);
        }
        return runFile(runner, args[0], out, err);
    }

    /** Presentation wiring: the processor with its injected help generators. */
    static CommandProcessor newCommandProcessor(ParkController park) {
        return new CommandProcessor(park,
                new HelpGenerator(CommandCatalog.commands(), new SyntaxHelpFormatter()),
                new HelpGenerator(CommandCatalog.commands(), new ExtendedHelpFormatter()));
    }

    private static int runInteractive(CommandRunner runner, InputStream stdin, PrintStream out, PrintStream err) {
        try {
            runner.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandRunner runner, String fileArg, PrintStream out, PrintStream err) {
        String prefix = "ERROR: cannot read command file '" + fileArg + "': ";
        Path file;
        try {
            file = Path.of(fileArg);
        } catch (InvalidPathException e) {
            err.println(prefix + "invalid path");
            return 1;
        }
        if (!Files.exists(file)) {
            err.println(prefix + "file does not exist");
            return 1;
        }
        if (!Files.isRegularFile(file)) {
            err.println(prefix + "not a regular file");
            return 1;
        }
        if (!Files.isReadable(file)) {
            err.println(prefix + "file is not readable");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            runner.run(reader, out);
            return 0;
        } catch (IOException | UncheckedIOException e) {
            err.println(prefix + e.getMessage());
            return 1;
        }
    }
}
