package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.amusementpark.cli.CommandProcessor;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserService;

/**
 * Application entry point.
 *
 * <p>Without arguments the application runs in interactive mode and reads commands from standard
 * input. With one argument it processes the commands contained in the given text file.
 */
public class App {

    private final PrintStream out;
    private final CommandProcessor processor;

    public App(PrintStream out) {
        this.out = out;
        this.processor = new CommandProcessor(new UserService(new UserRepository()));
    }

    public static void main(String[] args) {
        System.exit(execute(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, System.err));
    }

    /**
     * Runs the application and returns the process exit status.
     */
    static int execute(String[] args, BufferedReader stdin, PrintStream out, PrintStream err) {
        App app = new App(out);
        if (args.length == 0) {
            return app.runQuietly(stdin, err);
        }
        Path file = Path.of(args[0]);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
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
     * Processes every line of the reader in order until the end of input.
     */
    public void run(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
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
