package es.upm.amusementpark.presentation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.exception.DataAccessException;

/**
 * Application entry point.
 *
 * <p>Accepted arguments: none (interactive), {@code <file>} (commands from a file), {@code --fly}
 * and {@code --fly <file>} (same, using a temporary data directory deleted at the end).
 */
public class App {

    static final Path DATA_DIRECTORY = Path.of(".");
    static final Path FLY_ROOT = Path.of("tmp");
    static final String FLY = "--fly";

    private final PrintStream out;
    private final CommandProcessor processor;

    App(PrintStream out, CommandProcessor processor) {
        this.out = out;
        this.processor = processor;
    }

    public static void main(String[] args) {
        System.exit(execute(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, System.err, DATA_DIRECTORY, FLY_ROOT));
    }

    /**
     * Runs the application and returns the process exit status.
     */
    static int execute(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, Path dataDirectory,
            Path flyRoot) {
        boolean fly = args.length > 0 && args[0].equals(FLY);
        String[] rest = fly ? Arrays.copyOfRange(args, 1, args.length) : args;
        ParkController park = ParkController.getInstance();
        try {
            List<String> warnings = fly ? park.initializeTemporary(flyRoot) : park.initialize(dataDirectory);
            warnings.forEach(err::println);
        } catch (DataAccessException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
        try {
            CommandProcessor processor = new CommandProcessor(park,
                    new HelpGenerator(CommandCatalog.all(), new BasicHelpFormat()),
                    new HelpGenerator(CommandCatalog.all(), new ExtendedHelpFormat()));
            return run(rest, stdin, out, err, new App(out, processor));
        } finally {
            try {
                park.close();
            } catch (DataAccessException e) {
                err.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Runs with permanent data in {@code dataDirectory}; temporary data would go to its {@code tmp}.
     */
    static int execute(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, Path dataDirectory) {
        return execute(args, stdin, out, err, dataDirectory, dataDirectory.resolve("tmp"));
    }

    private static int run(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, App app) {
        if (args.length == 0) {
            return app.runQuietly(stdin, err);
        }
        Path file = Path.of(args[0]);
        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile(), StandardCharsets.UTF_8))) {
            return app.runQuietly(reader, err);
        } catch (IOException e) {
            err.println("Error: cannot read file '" + args[0] + "'");
            return 1;
        }
    }

    private int runQuietly(BufferedReader reader, PrintStream err) {
        try {
            run(reader);
            return 0;
        } catch (IOException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Processes every line of the reader in order until the end of input or {@code EXIT}.
     */
    public void run(BufferedReader reader) throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            processLine(line);
        }
    }

    /**
     * Handles a single command line and prints its response.
     */
    void processLine(String line) {
        String response = processor.process(line);
        if (response != null) {
            out.println(response);
        }
    }
}
