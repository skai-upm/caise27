package es.upm.parque;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.StandardInvoiceBuilder;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.PassRepository;
import es.upm.parque.persistence.StorageLocation;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.InputRunner;
import es.upm.parque.presentation.help.ExtendedHelpFormatter;
import es.upm.parque.presentation.help.HelpGenerator;
import es.upm.parque.presentation.help.SyntaxHelpFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Amusement Park application entry point.
 * Usage: [--fly] [commandFile]. Without a command file, commands are read from standard input.
 * With --fly, all persistence files live in a temporary tmp/&lt;timestamp&gt;/ directory removed at the end.
 */
public class App {

    static final String FLY_OPTION = "--fly";
    private static final DateTimeFormatter TMP_NAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        run(args, Path.of(""), System.out);
    }

    /**
     * Runs the application with persistence relative to the given base directory.
     */
    static void run(String[] args, Path baseDir, PrintStream out) {
        boolean fly = args.length > 0 && FLY_OPTION.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;

        Path dataDir = baseDir.resolve("data");
        Thread cleanup = null;
        if (fly) {
            try {
                dataDir = createTemporaryDirectory(baseDir.resolve("tmp"));
            } catch (IOException e) {
                out.println("ERROR cannot create temporary directory: " + e.getMessage());
                return;
            }
            Path tmp = dataDir;
            cleanup = new Thread(() -> deleteRecursively(tmp));
            Runtime.getRuntime().addShutdownHook(cleanup);
        }
        try {
            runWithData(commandFile, dataDir, out);
        } finally {
            if (fly) {
                deleteRecursively(dataDir);
                Runtime.getRuntime().removeShutdownHook(cleanup);
            }
        }
    }

    private static void runWithData(String commandFile, Path dataDir, PrintStream out) {
        InputRunner runner;
        try {
            runner = new InputRunner(new CommandProcessor(initialize(dataDir), createHelpGenerator()), out);
        } catch (IOException | RuntimeException e) {
            out.println("ERROR could not load stored data: " + e.getMessage());
            return;
        }
        if (commandFile == null) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            runner.run(stdin);
        } else {
            Path path = Path.of(commandFile);
            if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                out.println("ERROR cannot open command file: " + commandFile);
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                runner.run(reader);
            } catch (IOException e) {
                out.println("ERROR cannot read command file: " + commandFile);
            }
        }
    }

    /**
     * Composition root: creates the repositories for the given data directory, injects them into the
     * singleton controllers and wires the controllers together.
     */
    public static ParkController initialize(Path dataDir) throws IOException {
        return initialize(dataDir, new PasswordHasher());
    }

    /**
     * Same as {@link #initialize(Path)} with an explicit password hasher.
     */
    public static ParkController initialize(Path dataDir, PasswordHasher passwordHasher) throws IOException {
        StorageLocation storage = new StorageLocation(dataDir);
        UserController users = UserController.getInstance();
        users.configure(new UserRepository(storage.users()), passwordHasher);
        GroupController groups = GroupController.getInstance();
        groups.configure(new GroupRepository(storage.groups()), users);
        TicketController tickets = TicketController.getInstance();
        tickets.configure(new TicketRepository(storage.tickets(), storage.groupTickets()),
                new PassRepository(storage.passes()), users, groups);
        AttractionController attractions = AttractionController.getInstance();
        attractions.configure(new AttractionRepository(storage.attractions()));
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(tickets, StandardInvoiceBuilder::new);
        ParkController park = ParkController.getInstance();
        park.configure(users, groups, tickets, attractions, invoices);
        return park;
    }

    /**
     * Help generator with the formatting behavior injected for each help mode.
     */
    public static HelpGenerator createHelpGenerator() {
        return new HelpGenerator(Map.of(
                CommandProcessor.HELP_BASIC, new SyntaxHelpFormatter(),
                CommandProcessor.HELP_EXTENDED, new ExtendedHelpFormatter()));
    }

    static Path createTemporaryDirectory(Path tmpRoot) throws IOException {
        String name = LocalDateTime.now().format(TMP_NAME);
        Path dir = tmpRoot.resolve(name);
        for (int i = 1; Files.exists(dir); i++) {
            dir = tmpRoot.resolve(name + "_" + i);
        }
        return Files.createDirectories(dir);
    }

    static void deleteRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    System.err.println("WARNING could not delete " + p);
                }
            });
        } catch (IOException e) {
            System.err.println("WARNING could not delete " + dir);
        }
    }
}
