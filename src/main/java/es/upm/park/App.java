package es.upm.park;

import es.upm.park.business.controller.AttractionController;
import es.upm.park.business.controller.GroupController;
import es.upm.park.business.controller.InvoiceController;
import es.upm.park.business.controller.Park;
import es.upm.park.business.controller.PassController;
import es.upm.park.business.controller.TicketController;
import es.upm.park.business.controller.UserController;
import es.upm.park.business.security.BCryptPasswordHasher;
import es.upm.park.business.security.PasswordHasher;
import es.upm.park.persistence.DataPaths;
import es.upm.park.persistence.PersistenceException;
import es.upm.park.persistence.Repositories;
import es.upm.park.persistence.file.FileRepositories;
import es.upm.park.presentation.CommandProcessor;
import es.upm.park.presentation.CommandRunner;
import es.upm.park.presentation.help.ExtendedHelpFormatter;
import es.upm.park.presentation.help.HelpGenerator;
import es.upm.park.presentation.help.SyntaxHelpFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Application entry point and composition root: creates the repositories
 * for the chosen data directory, injects them and the controller
 * collaborations into the singletons, and builds the presentation layer.
 * Accepted forms:
 * <pre>
 * java -jar app.jar                 interactive, permanent data
 * java -jar app.jar input.txt       command file, permanent data
 * java -jar app.jar --fly           interactive, temporary data
 * java -jar app.jar --fly input.txt command file, temporary data
 * </pre>
 */
public final class App {

    static final String DATA_DIR_PROPERTY = "park.data.dir";
    static final String DEFAULT_DATA_DIR = ".";
    static final String FLY_OPTION = "--fly";
    static final String TMP_ROOT_PROPERTY = "park.tmp.root";
    static final String DEFAULT_TMP_ROOT = "tmp";
    private static final DateTimeFormatter FLY_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private App() {
    }

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, new BCryptPasswordHasher());
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Configures every singleton for the given repositories and loads the
     * stored park state.
     */
    public static CommandProcessor.Controllers initialize(Repositories repositories, PasswordHasher hasher) {
        Park park = Park.getInstance();
        park.configure(repositories);
        park.load();

        PassController passes = PassController.getInstance();
        passes.configure(park);
        UserController users = UserController.getInstance();
        users.configure(park, hasher);
        GroupController groups = GroupController.getInstance();
        groups.configure(park);
        TicketController tickets = TicketController.getInstance();
        tickets.configure(park, passes);
        AttractionController attractions = AttractionController.getInstance();
        attractions.configure(park, passes);
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(park, passes);
        return new CommandProcessor.Controllers(park, users, groups, tickets, passes, attractions, invoices);
    }

    public static CommandProcessor createCommandProcessor(CommandProcessor.Controllers controllers) {
        return new CommandProcessor(controllers,
                new HelpGenerator(new SyntaxHelpFormatter()),
                new HelpGenerator(new ExtendedHelpFormatter()));
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, PasswordHasher hasher) {
        boolean fly = args.length > 0 && FLY_OPTION.equals(args[0]);
        int fileArgument = fly ? 1 : 0;
        Path commandFile = args.length > fileArgument ? Path.of(args[fileArgument]) : null;
        if (commandFile != null && (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile))) {
            err.println("ERROR: command file not found or not readable: " + commandFile);
            return 1;
        }

        Path tmpRoot = Path.of(System.getProperty(TMP_ROOT_PROPERTY, DEFAULT_TMP_ROOT));
        boolean tmpRootExisted = Files.isDirectory(tmpRoot);
        Path dataDir;
        try {
            dataDir = fly ? createFlyDirectory(tmpRoot)
                    : Path.of(System.getProperty(DATA_DIR_PROPERTY, DEFAULT_DATA_DIR));
        } catch (IOException e) {
            err.println("ERROR: cannot create temporary data directory: " + e.getMessage());
            return 1;
        }

        try {
            return execute(dataDir, commandFile, in, out, err, hasher);
        } finally {
            if (fly) {
                deleteRecursively(dataDir, err);
                if (!tmpRootExisted) {
                    deleteIfEmpty(tmpRoot);
                }
            }
        }
    }

    private static int execute(Path dataDir, Path commandFile, InputStream in, PrintStream out, PrintStream err,
                               PasswordHasher hasher) {
        CommandProcessor processor;
        try {
            processor = createCommandProcessor(
                    initialize(FileRepositories.create(new DataPaths(dataDir)), hasher));
        } catch (PersistenceException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(processor, out);

        if (commandFile == null) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                return 0;
            } catch (IOException e) {
                err.println("ERROR: cannot read standard input: " + e.getMessage());
                return 1;
            }
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read command file " + commandFile + ": " + e.getMessage());
            return 1;
        }
    }

    /**
     * Creates {@code <tmpRoot>/yyyyMMdd_HHmmss}, adding a numeric suffix if
     * a directory with that timestamp already exists.
     */
    static Path createFlyDirectory(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String stamp = LocalDateTime.now().format(FLY_DIR_FORMAT);
        Path candidate = tmpRoot.resolve(stamp);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = tmpRoot.resolve(stamp + "_" + suffix++);
        }
        return Files.createDirectory(candidate);
    }

    private static void deleteRecursively(Path directory, PrintStream err) {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            err.println("WARNING: could not delete temporary directory " + directory + ": " + e.getMessage());
        }
    }

    private static void deleteIfEmpty(Path directory) {
        try (Stream<Path> entries = Files.list(directory)) {
            if (entries.findAny().isEmpty()) {
                Files.delete(directory);
            }
        } catch (IOException e) {
            // Leaving an empty tmp/ directory behind is harmless.
        }
    }
}
