package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Starts the application.
 * <ul>
 *   <li>Without arguments commands are read from the terminal; otherwise the first argument is the path of a
 *   file with one command per line.</li>
 *   <li>With {@code --fly} as first argument all data is kept in a temporary directory under {@code tmp/}
 *   that is deleted when the application ends; the command file, if any, is the next argument.</li>
 * </ul>
 */
public class App {

    private static final Path DATA_DIR = Path.of("data");
    private static final Path FLY_BASE_DIR = Path.of("tmp");
    private static final String FLY_OPTION = "--fly";

    public static void main(String[] args) {
        boolean fly = args.length > 0 && FLY_OPTION.equals(args[0]);
        int fileArgument = fly ? 1 : 0;
        String commandFile = args.length > fileArgument ? args[fileArgument] : null;

        if (!fly) {
            run(DATA_DIR, commandFile);
            return;
        }
        TemporaryDataDirectory temporary;
        try {
            temporary = TemporaryDataDirectory.create(FLY_BASE_DIR, LocalDateTime.now());
        } catch (IOException e) {
            System.out.println("ERROR Could not create temporary data directory: " + e.getMessage());
            return;
        }
        Thread cleanup = new Thread(temporary::delete);
        Runtime.getRuntime().addShutdownHook(cleanup);
        try {
            run(temporary.getPath(), commandFile);
        } finally {
            temporary.delete();
            Runtime.getRuntime().removeShutdownHook(cleanup);
        }
    }

    private static void run(Path dataDir, String commandFile) {
        CommandProcessor processor;
        try {
            processor = buildProcessor(dataDir);
        } catch (IOException e) {
            System.out.println("ERROR Could not load data: " + e.getMessage());
            return;
        }
        CommandRunner runner = new CommandRunner(processor, System.out);
        if (commandFile == null) {
            runInteractive(runner);
        } else {
            runFile(runner, commandFile);
        }
    }

    /**
     * Builds the command processor with every repository stored in {@code dataDir}.
     */
    static CommandProcessor buildProcessor(Path dataDir) throws IOException {
        UserManager userManager = new UserManager(new UserRepository(dataDir.resolve("users.txt")));
        GroupManager groupManager = new GroupManager(new GroupRepository(dataDir.resolve("groups.txt")), userManager);
        SystemDate systemDate = SystemDate.today();
        TicketManager ticketManager = new TicketManager(
                new TicketRepository(dataDir.resolve("tickets.txt"), dataDir.resolve("grouptickets.txt")),
                userManager, groupManager, systemDate);
        AttractionManager attractionManager = new AttractionManager(
                new AttractionRepository(dataDir.resolve("attractions.txt")), ticketManager, systemDate);
        return new CommandProcessor(userManager, groupManager, ticketManager, attractionManager, systemDate);
    }

    private static void runInteractive(CommandRunner runner) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            runner.run(reader);
        } catch (IOException e) {
            System.out.println("ERROR Could not read input: " + e.getMessage());
        }
    }

    private static void runFile(CommandRunner runner, String path) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (NoSuchFileException e) {
            System.out.println("ERROR Cannot read command file '" + path + "': file does not exist");
        } catch (IOException | InvalidPathException e) {
            System.out.println("ERROR Cannot read command file '" + path + "': " + e.getMessage());
        }
    }
}
