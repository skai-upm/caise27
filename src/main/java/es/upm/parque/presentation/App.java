package es.upm.parque.presentation;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.DefaultInvoiceBuilder;
import es.upm.parque.business.invoice.InvoiceDirector;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.security.BCryptPasswordEncoder;
import es.upm.parque.persistence.FileAttractionRepository;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FileTicketRepository;
import es.upm.parque.persistence.FileUserRepository;
import es.upm.parque.persistence.TemporaryDataDirectory;
import es.upm.parque.presentation.help.CommandCatalog;
import es.upm.parque.presentation.help.ConciseHelpFormat;
import es.upm.parque.presentation.help.ExtendedHelpFormat;
import es.upm.parque.presentation.help.HelpGenerator;

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
import java.util.Map;

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
        CommandProcessor processor = initialize(dataDir);
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

    /**
     * Application initialization: creates the repositories for the given data directory (normal or --fly) and
     * injects them, together with the collaborators each controller needs, into the singleton controllers.
     */
    static CommandProcessor initialize(Path dataDir) {
        SystemDate systemDate = new SystemDate();
        UserController userController = UserController.getInstance()
                .configure(new FileUserRepository(dataDir.resolve("users.txt")), new BCryptPasswordEncoder());
        GroupController groupController = GroupController.getInstance()
                .configure(new FileGroupRepository(dataDir.resolve("groups.txt")), userController);
        TicketController ticketController = TicketController.getInstance().configure(
                new FileTicketRepository(dataDir.resolve("tickets.txt"), dataDir.resolve("grouptickets.txt")),
                userController, groupController, systemDate);
        AttractionController attractionController = AttractionController.getInstance().configure(
                new FileAttractionRepository(dataDir.resolve("attractions.txt")), ticketController, systemDate);
        InvoiceController invoiceController = InvoiceController.getInstance().configure(userController,
                groupController, ticketController, systemDate, new DefaultInvoiceBuilder(), new InvoiceDirector());
        CommandCatalog catalog = CommandCatalog.parkCommands();
        Map<String, HelpGenerator> helpGenerators = Map.of(
                "", new HelpGenerator(catalog, new ConciseHelpFormat()),
                "EXTENDED", new HelpGenerator(catalog, new ExtendedHelpFormat()));
        return new CommandProcessor(userController, groupController, ticketController, attractionController,
                invoiceController, systemDate, helpGenerators);
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
