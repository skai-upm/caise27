package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard input; with one argument
 * they are read from the given text file.
 */
public class App {

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, Path.of(UserRepository.DEFAULT_FILE));
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, Path usersFile) {
        UserRepository repository = new UserRepository(usersFile);
        try {
            repository.load();
        } catch (UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(new UserService(repository)), out);

        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                return 0;
            } catch (IOException e) {
                out.println("ERROR: cannot read standard input: " + e.getMessage());
                return 1;
            }
        }

        return runner.runFile(Path.of(args[0])) ? 0 : 1;
    }
}
