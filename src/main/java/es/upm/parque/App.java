package es.upm.parque;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.TextInvoiceBuilder;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.TemporaryDataDirectory;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.CommandRunner;
import es.upm.parque.presentation.help.CommandCatalog;
import es.upm.parque.presentation.help.ExtendedHelpFormatter;
import es.upm.parque.presentation.help.HelpGenerator;
import es.upm.parque.presentation.help.SyntaxHelpFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

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
    private static final int PASSWORD_COST = 10;

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
     * Creates the repositories for {@code dataDir} and injects them, together with the other collaborators, into the
     * single instance of each controller. Returns the command processor wired to those controllers.
     */
    static CommandProcessor buildProcessor(Path dataDir) throws IOException {
        UserRepository userRepository = new UserRepository(dataDir.resolve("users.txt"));
        GroupRepository groupRepository = new GroupRepository(dataDir.resolve("groups.txt"));
        TicketRepository ticketRepository =
                new TicketRepository(dataDir.resolve("tickets.txt"), dataDir.resolve("grouptickets.txt"));
        AttractionRepository attractionRepository = new AttractionRepository(dataDir.resolve("attractions.txt"));
        SystemDate systemDate = SystemDate.today();

        UserController userController =
                UserController.getInstance().configure(userRepository, new PasswordHasher(PASSWORD_COST));
        GroupController groupController = GroupController.getInstance().configure(groupRepository, userController);
        TicketController ticketController = TicketController.getInstance()
                .configure(ticketRepository, userController, groupController, systemDate);
        AttractionController attractionController = AttractionController.getInstance()
                .configure(attractionRepository, ticketController, systemDate);
        InvoiceController invoiceController = InvoiceController.getInstance()
                .configure(userController, groupController, ticketController, new TextInvoiceBuilder());
        HelpGenerator helpGenerator = new HelpGenerator(CommandCatalog.standard(),
                Map.of("", new SyntaxHelpFormatter(), "EXTENDED", new ExtendedHelpFormatter()));
        return new CommandProcessor(userController, groupController, ticketController, attractionController,
                invoiceController, systemDate, helpGenerator);
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
