package es.upm.parque;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.parque.command.CommandProcessor;
import es.upm.parque.persistence.UserFileStore;
import es.upm.parque.repository.UserRepository;

/**
 * Application entry point. Commands are read from the file given as first argument or,
 * without arguments, from standard input; both sources share the same processing.
 */
public class App {

    static final String USERS_FILE = "users.txt";

    /** UTF-8 console output on the process's standard output; intentionally never closed. */
    private static final PrintStream CONSOLE =
            new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);

    public static void main(String[] args) throws IOException {
        UserRepository users = loadUsers(CONSOLE);
        if (users != null) {
            start(args, new CommandProcessor(users), System.in, CONSOLE);
        }
    }

    /** Loads the stored users, reporting ignored lines; returns {@code null} if the users file cannot be read. */
    private static UserRepository loadUsers(PrintStream console) {
        UserRepository users;
        try {
            users = new UserRepository(new UserFileStore(Path.of(USERS_FILE)));
        } catch (UncheckedIOException e) {
            console.println("ERROR: cannot read users file '" + USERS_FILE + "'");
            return null;
        }
        if (users.getIgnoredOnLoad() > 0) {
            console.println("WARNING: " + users.getIgnoredOnLoad() + " invalid line(s) in " + USERS_FILE + " were ignored");
        }
        return users;
    }

    /** Selects the command source from the arguments and processes every command from it. */
    static void start(String[] args, CommandProcessor processor, InputStream stdin, PrintStream output)
            throws IOException {
        if (args.length == 0) {
            run(processor, new InputStreamReader(stdin, StandardCharsets.UTF_8), output);
            return;
        }
        String path = args[0];
        try (Reader file = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            run(processor, file, output);
        } catch (IOException e) {
            output.println("ERROR: cannot read command file '" + path + "'");
        }
    }

    static void run(CommandProcessor processor, Reader input, PrintStream output) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            String response = processor.process(line);
            if (response != null) {
                output.println(response);
            }
        }
    }
}
