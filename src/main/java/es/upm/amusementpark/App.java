package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard
 * input; with an argument, the first one is the path of a command file. Both
 * modes feed the same {@link CommandProcessor}.
 */
public class App {

    static final Path DEFAULT_DATA_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        run(args, System.in, System.out, System.err, DEFAULT_DATA_FILE);
    }

    /**
     * Runs the application and returns its exit status: 0 on normal
     * termination, 1 if the command file or the user data could not be read.
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path dataFile) {
        UserManager userManager;
        try {
            userManager = new UserManager(new UserRepository(dataFile));
        } catch (IOException e) {
            err.println("ERROR: cannot load users from " + dataFile + ": " + e.getMessage());
            return 1;
        }
        CommandProcessor processor = new CommandProcessor(userManager, out);

        if (args.length == 0) {
            return execute(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)),
                    processor, err, "standard input");
        }

        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            err.println("ERROR: cannot read file " + commandFile + ": file does not exist or is not readable");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            return execute(reader, processor, err, commandFile.toString());
        } catch (IOException e) {
            err.println("ERROR: cannot read file " + commandFile + ": " + e.getMessage());
            return 1;
        }
    }

    private static int execute(BufferedReader reader, CommandProcessor processor,
                               PrintStream err, String source) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!processor.process(line)) {
                    break;
                }
            }
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read " + source + ": " + e.getMessage());
            return 1;
        }
    }
}
