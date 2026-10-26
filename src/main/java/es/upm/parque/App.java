package es.upm.parque;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.MainController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.StandardInvoiceBuilder;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.DataDirectory;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.ExecutionOptions;
import es.upm.parque.presentation.help.ExtendedHelpFormatter;
import es.upm.parque.presentation.help.HelpGenerator;
import es.upm.parque.presentation.help.SyntaxHelpFormatter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;

/**
 * Entry point. Without a file argument commands are read interactively from standard input;
 * otherwise the file's lines are executed as commands. Both modes share the same processing.
 * With {@code --fly}, every repository works on a temporary directory deleted at the end of the run.
 */
public class App {

    static final String USERS_FILE = "users.txt";
    static final String GROUPS_FILE = "groups.txt";
    static final String TICKETS_FILE = "tickets.txt";
    static final String GROUP_TICKETS_FILE = "grouptickets.txt";
    static final String ATTRACTIONS_FILE = "attractions.txt";

    private final CommandProcessor processor;

    public App(CommandProcessor processor) {
        this.processor = processor;
    }

    public static void main(String[] args) {
        execute(args, Path.of(""), Clock.systemDefaultZone(), System.in, System.out);
    }

    /**
     * Runs the application: parses the options, chooses the data directory (permanent, or a new
     * temporary one for {@code --fly}), processes the commands and removes the temporary directory.
     */
    public static void execute(String[] args, Path baseDir, Clock clock, InputStream stdin, PrintStream out) {
        ExecutionOptions options = ExecutionOptions.parse(args);
        if (!options.fly()) {
            run(options, baseDir.resolve(DataDirectory.PERMANENT), clock, stdin, out);
            return;
        }
        Path dataDir = DataDirectory.createTemporary(baseDir, clock);
        Thread cleanup = new Thread(() -> DataDirectory.deleteRecursively(dataDir));
        Runtime.getRuntime().addShutdownHook(cleanup);
        try {
            run(options, dataDir, clock, stdin, out);
        } finally {
            DataDirectory.deleteRecursively(dataDir);
            Runtime.getRuntime().removeShutdownHook(cleanup);
        }
    }

    private static void run(ExecutionOptions options, Path dataDir, Clock clock, InputStream stdin, PrintStream out) {
        new App(configure(dataDir, clock)).start(options.inputFile(), stdin, out);
    }

    /**
     * Composition root: creates the repositories for the given data directory, injects them and the
     * collaborating controllers into the singleton controllers, and returns the command processor.
     * Normal and --fly executions only differ in the directory passed here.
     */
    public static CommandProcessor configure(Path dataDir, Clock clock) {
        MainController mainController = MainController.getInstance();
        mainController.configure(clock);
        UserController userController = UserController.getInstance();
        userController.configure(new UserRepository(dataDir.resolve(USERS_FILE)));
        GroupController groupController = GroupController.getInstance();
        groupController.configure(new GroupRepository(dataDir.resolve(GROUPS_FILE)), userController);
        TicketController ticketController = TicketController.getInstance();
        ticketController.configure(new TicketRepository(dataDir.resolve(TICKETS_FILE), dataDir.resolve(GROUP_TICKETS_FILE)),
                userController, groupController, mainController);
        AttractionController attractionController = AttractionController.getInstance();
        attractionController.configure(new AttractionRepository(dataDir.resolve(ATTRACTIONS_FILE)), ticketController,
                mainController);
        InvoiceController invoiceController = InvoiceController.getInstance();
        invoiceController.configure(userController, groupController, ticketController, StandardInvoiceBuilder::new);
        Map<String, HelpGenerator> help = Map.of(
                "HELP", new HelpGenerator(CommandProcessor.COMMANDS, new SyntaxHelpFormatter()),
                "HELP EXTENDED", new HelpGenerator(CommandProcessor.COMMANDS, new ExtendedHelpFormatter()));
        return new CommandProcessor(userController, groupController, ticketController, attractionController,
                invoiceController, mainController, help);
    }

    /**
     * Reads commands from the given file, or from standard input when {@code inputFile} is null.
     */
    public void start(String inputFile, InputStream stdin, PrintStream out) {
        if (inputFile == null) {
            run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            return;
        }
        Path file = Path.of(inputFile);
        if (Files.notExists(file)) {
            out.println("ERROR Input file not found: " + file);
            return;
        }
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            run(in, out);
        } catch (IOException e) {
            out.println("ERROR Cannot read input file: " + file);
        }
    }

    /**
     * Processes every non-blank line until the input ends or an EXIT command is processed.
     */
    public void run(BufferedReader in, PrintStream out) {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                out.println(processor.process(line));
                if (processor.isExitRequested()) {
                    return;
                }
            }
        } catch (IOException e) {
            out.println("ERROR Cannot read input: " + e.getMessage());
        }
    }
}
