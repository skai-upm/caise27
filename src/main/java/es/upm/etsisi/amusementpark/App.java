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
import es.upm.etsisi.amusementpark.persistence.UserStorage;
import es.upm.etsisi.amusementpark.user.UserManager;

/**
 * Application entry point. Without arguments commands are read from standard
 * input; with an argument, from the text file at that path.
 */
public class App {
    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, Path usersFile) {
        UserStorage storage = new UserStorage(usersFile);
        UserManager users;
        try {
            users = new UserManager(storage);
        } catch (IOException e) {
            out.println("ERROR: cannot load users from " + usersFile + ": " + e.getMessage());
            return 1;
        }
        for (String warning : storage.getWarnings()) {
            out.println("WARNING: " + warning);
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(users), out);
        if (args.length > 0) {
            return runner.runFile(Path.of(args[0])) ? 0 : 1;
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
