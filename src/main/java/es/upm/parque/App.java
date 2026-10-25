package es.upm.parque;

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
import java.time.LocalDate;

/**
 * Starts the application. Without arguments commands are read from standard input;
 * with an argument they are read from the file it names. Both modes share the same processing.
 */
public class App {

    static final Path DATA_DIR = Path.of("data");

    public static void main(String[] args) {
        start(args, System.in, System.out, DATA_DIR);
    }

    static void start(String[] args, InputStream stdin, PrintStream out, Path dataDir) {
        CommandProcessor processor;
        try {
            UserManager users = new UserManager(new UserRepository(dataDir.resolve("users.txt")));
            GroupManager groups = new GroupManager(new GroupRepository(dataDir.resolve("groups.txt")), users);
            SystemDate systemDate = new SystemDate(LocalDate.now());
            TicketManager tickets = new TicketManager(new TicketRepository(dataDir.resolve("tickets.txt")), users,
                    systemDate);
            processor = new CommandProcessor(users, groups, tickets, systemDate);
        } catch (IOException e) {
            out.println("ERROR " + e.getMessage());
            return;
        }
        if (args.length == 0) {
            try {
                run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out, processor);
            } catch (IOException e) {
                out.println("ERROR CANNOT READ STANDARD INPUT");
            }
            return;
        }
        String path = args[0];
        try (BufferedReader file = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            run(file, out, processor);
        } catch (IOException | UncheckedIOException | InvalidPathException e) {
            out.println("ERROR CANNOT READ FILE " + path);
        }
    }

    static void run(BufferedReader in, PrintStream out, CommandProcessor processor) throws IOException {
        String line;
        while (!processor.isFinished() && (line = in.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
