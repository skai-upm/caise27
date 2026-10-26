package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Entry point of the amusement-park application. Without a file argument commands are read from the terminal;
 * with one they are read from the given file. Both sources share the same processing. The {@code --fly} option
 * keeps every data file in a temporary directory that is deleted when the run ends.
 */
public class App {
    static final String FLY_OPTION = "--fly";
    private static final Path DATA_DIR = Path.of("data");
    private static final Path TMP_ROOT = Path.of("tmp");

    public static void main(String[] args) {
        run(args, System.in, System.out, DATA_DIR, TMP_ROOT);
    }

    static void run(String[] args, InputStream stdin, PrintStream out, Path dataDir, Path tmpRoot) {
        boolean fly = false;
        String commandFile = null;
        for (String arg : args) {
            if (arg.equals(FLY_OPTION)) {
                fly = true;
            } else if (arg.startsWith("--")) {
                out.println("ERROR: unknown option " + arg + " (supported: " + FLY_OPTION + ")");
                return;
            } else if (commandFile == null) {
                commandFile = arg;
            }
        }
        if (!fly) {
            execute(commandFile, stdin, out, dataDir);
            return;
        }
        TemporaryDataDirectory temporary = TemporaryDataDirectory.create(tmpRoot, LocalDateTime.now());
        Thread cleanup = new Thread(temporary::delete);
        Runtime.getRuntime().addShutdownHook(cleanup);
        try {
            execute(commandFile, stdin, out, temporary.getPath());
        } finally {
            temporary.delete();
            Runtime.getRuntime().removeShutdownHook(cleanup);
        }
    }

    private static void execute(String commandFile, InputStream stdin, PrintStream out, Path dataDir) {
        UserManager userManager = new UserManager(new UserRepository(dataDir.resolve("users.txt")));
        GroupManager groupManager = new GroupManager(new GroupRepository(dataDir.resolve("groups.txt")), userManager);
        SystemDate systemDate = new SystemDate();
        TicketManager ticketManager = new TicketManager(
                new TicketRepository(dataDir.resolve("tickets.txt"), dataDir.resolve("grouptickets.txt")),
                userManager, groupManager, systemDate);
        AttractionManager attractionManager = new AttractionManager(
                new AttractionRepository(dataDir.resolve("attractions.txt")), ticketManager, systemDate);
        CommandProcessor processor =
                new CommandProcessor(userManager, groupManager, ticketManager, attractionManager, systemDate);
        if (commandFile == null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8))) {
                processLines(in, processor, out);
            } catch (IOException e) {
                out.println("ERROR: cannot read standard input: " + e.getMessage());
            }
            return;
        }
        Path file = Path.of(commandFile);
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            processLines(in, processor, out);
        } catch (NoSuchFileException e) {
            out.println("ERROR: command file not found: " + file);
        } catch (AccessDeniedException e) {
            out.println("ERROR: command file cannot be opened: " + file);
        } catch (IOException e) {
            out.println("ERROR: command file cannot be read: " + file + " (" + e.getMessage() + ")");
        }
    }

    private static void processLines(BufferedReader in, CommandProcessor processor, PrintStream out)
            throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = in.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
