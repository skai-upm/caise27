package es.upm.amusementpark;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.command.CommandRunner;
import es.upm.amusementpark.persistence.FileUserRepository;
import es.upm.amusementpark.service.UserService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Entry point of the amusement park application. Without arguments it reads
 * commands from standard input; with an argument it reads them from that file.
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, Path.of(FileUserRepository.DEFAULT_FILE_NAME));
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application and returns the process exit status.
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path usersFile) {
        UserService userService;
        try {
            userService = new UserService(new FileUserRepository(usersFile, err));
        } catch (IOException e) {
            out.println("ERROR: Cannot load users from " + usersFile + ": " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(userService), out);

        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                return 0;
            } catch (IOException e) {
                out.println("ERROR: Cannot read standard input: " + e.getMessage());
                return 1;
            }
        }

        String fileName = args[0];
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName), StandardCharsets.UTF_8)) {
            runner.run(reader);
            return 0;
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR: Cannot read command file '" + fileName + "'");
            return 1;
        }
    }
}
