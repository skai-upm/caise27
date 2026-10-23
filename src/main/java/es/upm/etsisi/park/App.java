package es.upm.etsisi.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.etsisi.park.cli.CommandProcessor;
import es.upm.etsisi.park.persistence.UserRepository;
import es.upm.etsisi.park.user.UserService;

/**
 * Entry point of the amusement park application.
 * Without arguments commands are read from standard input; with one argument
 * they are read from the given text file.
 */
public class App {

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Runs the application and returns the process exit status. */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        UserService users;
        UserRepository repository = UserRepository.fromSystemProperty(err);
        try {
            users = new UserService(repository);
        } catch (IOException e) {
            err.println("ERROR: Could not load users from " + repository.getFile() + ": " + e.getMessage());
            return 1;
        }
        CommandProcessor processor = new CommandProcessor(users, out);

        if (args.length == 0) {
            out.println("Amusement park - type HELP for the list of commands, EXIT to quit");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                processAll(reader, processor);
            } catch (IOException e) {
                err.println("ERROR: Could not read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            err.println("ERROR: Command file '" + args[0] + "' does not exist or cannot be read");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            processAll(reader, processor);
        } catch (IOException e) {
            err.println("ERROR: Could not read command file '" + args[0] + "': " + e.getMessage());
            return 1;
        }
        return 0;
    }

    private static void processAll(BufferedReader reader, CommandProcessor processor) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!processor.process(line)) {
                return;
            }
        }
    }
}
