package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.business.security.PasswordHasher;
import es.upm.etsisi.amusementpark.persistence.Storage;
import es.upm.etsisi.amusementpark.persistence.TextAttractionRepository;
import es.upm.etsisi.amusementpark.persistence.TextGroupRepository;
import es.upm.etsisi.amusementpark.persistence.TextPassRepository;
import es.upm.etsisi.amusementpark.persistence.TextTicketRepository;
import es.upm.etsisi.amusementpark.persistence.TextUserRepository;
import es.upm.etsisi.amusementpark.presentation.BasicHelpFormat;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.ExtendedHelpFormat;
import es.upm.etsisi.amusementpark.presentation.HelpGenerator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Application entry point and composition root: it creates the repositories for the selected
 * storage, injects them into the singleton controllers and wires the presentation layer.
 * Supported forms: no arguments (interactive), {@code <file>},
 * {@code --fly} and {@code --fly <file>}. With --fly all data lives in a temporary directory
 * that is deleted when execution ends.
 */
public class App {

    static final String FLY = "--fly";
    private static final DateTimeFormatter FLY_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        System.exit(run(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, System.err, Path.of("")));
    }

    /**
     * Runs the application with data stored under {@code workDir} and returns the process exit status.
     */
    static int run(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, Path workDir) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        int fileArg = fly ? 1 : 0;
        String commandFile = args.length > fileArg ? args[fileArg] : null;
        Path flyDir = null;
        try {
            Path dataDir = workDir;
            if (fly) {
                flyDir = createFlyDirectory(workDir.resolve("tmp"));
                dataDir = flyDir;
            }
            CommandProcessor processor = bootstrap(new Storage(dataDir), LocalDate.now(), new BCryptPasswordHasher());
            if (commandFile == null) {
                return execute(processor, stdin, out, err);
            }
            Path file = Path.of(commandFile);
            if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
                err.println("ERROR: command file not found or not readable: " + file);
                return 1;
            }
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                return execute(processor, reader, out, err);
            }
        } catch (IOException | RuntimeException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        } finally {
            if (flyDir != null) {
                deleteFlyDirectory(flyDir, err);
            }
        }
    }

    /**
     * Creates the text-file repositories for the storage, injects them and the collaborations into
     * the controllers, and returns the command processor with its injected help generators.
     */
    public static CommandProcessor bootstrap(Storage storage, LocalDate today, PasswordHasher hasher) {
        UserController users = UserController.getInstance();
        users.configure(new TextUserRepository(storage.usersFile()), hasher);
        PassController passes = PassController.getInstance();
        passes.configure(new TextPassRepository(storage.passesFile()));
        ParkController park = ParkController.getInstance();
        park.configure(today, users, passes, new TextGroupRepository(storage.groupsFile()),
                new TextTicketRepository(storage.ticketsFile()), new TextAttractionRepository(storage.attractionsFile()));
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(users, park, passes);
        return new CommandProcessor(park, invoices, new HelpGenerator(new BasicHelpFormat()),
                new HelpGenerator(new ExtendedHelpFormat()));
    }

    private static int execute(CommandProcessor processor, BufferedReader input, PrintStream out, PrintStream err) {
        try {
            processor.run(input, out);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read commands: " + e.getMessage());
            return 1;
        }
    }

    static Path createFlyDirectory(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String stamp = LocalDateTime.now().format(FLY_DIR_FORMAT);
        Path dir = tmpRoot.resolve(stamp);
        for (int n = 1; Files.exists(dir); n++) {
            dir = tmpRoot.resolve(stamp + "_" + n);
        }
        return Files.createDirectory(dir);
    }

    private static void deleteFlyDirectory(Path dir, PrintStream err) {
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
            Path parent = dir.getParent();
            try (Stream<Path> rest = Files.list(parent)) {
                if (rest.findAny().isEmpty()) {
                    Files.delete(parent);
                }
            }
        } catch (IOException e) {
            err.println("WARNING: could not delete temporary directory " + dir + ": " + e.getMessage());
        }
    }
}
