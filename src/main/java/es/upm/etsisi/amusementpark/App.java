package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.ParkServices;
import es.upm.etsisi.amusementpark.business.service.PassManager;
import es.upm.etsisi.amusementpark.persistence.PassFileRepository;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceDirector;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.business.service.AttractionManager;
import es.upm.etsisi.amusementpark.business.service.GroupManager;
import es.upm.etsisi.amusementpark.business.service.TicketManager;
import es.upm.etsisi.amusementpark.business.service.UserManager;
import es.upm.etsisi.amusementpark.persistence.AttractionFileRepository;
import es.upm.etsisi.amusementpark.persistence.GroupFileRepository;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.TicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.UserFileRepository;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.CommandRunner;
import es.upm.etsisi.amusementpark.presentation.help.CommandCatalog;
import es.upm.etsisi.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;
import es.upm.etsisi.amusementpark.presentation.help.SyntaxHelpFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Entry point. Without arguments, commands are read from standard input
 * (interactive mode); with an argument, the first one is the path of a command
 * file (file mode). Both modes share the same {@link CommandRunner}.
 * A leading {@value #FLY_OPTION} argument redirects all persistence to a
 * temporary per-run directory that is deleted when the run ends.
 */
public class App {

    static final String USERS_FILE_NAME = "users.txt";
    static final Path USERS_FILE = Path.of(USERS_FILE_NAME);
    static final String GROUPS_FILE_NAME = "groups.txt";
    static final String TICKETS_FILE_NAME = "tickets.txt";
    static final String GROUP_TICKETS_FILE_NAME = "grouptickets.txt";
    static final String ATTRACTIONS_FILE_NAME = "attractions.txt";
    static final String PASSES_FILE_NAME = "passes.txt";
    static final String FLY_OPTION = "--fly";
    static final String TMP_DIR_NAME = "tmp";
    private static final DateTimeFormatter RUN_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application.
     *
     * @param usersFile permanent users file; the other data files ({@value #GROUPS_FILE_NAME},
     *                  {@value #TICKETS_FILE_NAME}, {@value #GROUP_TICKETS_FILE_NAME},
     *                  {@value #ATTRACTIONS_FILE_NAME}) are in the same directory. With
     *                  {@value #FLY_OPTION}, a fresh {@value #TMP_DIR_NAME}/&lt;timestamp&gt; directory next
     *                  to it is used instead.
     * @return 0 on normal termination, 1 if the stored data or the command file could not be read
     */
    static int run(String[] args, InputStream stdin, PrintStream out, Path usersFile) {
        if (args.length == 0 || !FLY_OPTION.equals(args[0])) {
            return runWith(args, stdin, out, usersFile);
        }
        Path flyDir;
        try {
            flyDir = createFlyDirectory(usersFile.toAbsolutePath().resolveSibling(TMP_DIR_NAME));
        } catch (IOException e) {
            out.println("ERROR: Could not create the temporary data directory: " + e);
            return 1;
        }
        Thread cleanupOnShutdown = new Thread(() -> deleteQuietly(flyDir));
        Runtime.getRuntime().addShutdownHook(cleanupOnShutdown);
        try {
            return runWith(Arrays.copyOfRange(args, 1, args.length), stdin, out, flyDir.resolve(USERS_FILE_NAME));
        } finally {
            if (!deleteQuietly(flyDir)) {
                out.println("ERROR: Could not delete temporary directory '" + flyDir + "'");
            }
            removeHook(cleanupOnShutdown);
        }
    }

    private static int runWith(String[] args, InputStream stdin, PrintStream out, Path usersFile) {
        CommandRunner runner;
        try {
            UserManager users = new UserManager(new UserFileRepository(usersFile),
                    new BCryptPasswordHasher(BCryptPasswordHasher.DEFAULT_LOG_ROUNDS));
            GroupManager groups = new GroupManager(
                    new GroupFileRepository(usersFile.resolveSibling(GROUPS_FILE_NAME)), users);
            TicketManager tickets = new TicketManager(
                    new TicketFileRepository(usersFile.resolveSibling(TICKETS_FILE_NAME)),
                    new GroupTicketFileRepository(usersFile.resolveSibling(GROUP_TICKETS_FILE_NAME)), users, groups);
            AttractionManager attractions = new AttractionManager(
                    new AttractionFileRepository(usersFile.resolveSibling(ATTRACTIONS_FILE_NAME)));
            PassManager passes = new PassManager(
                    new PassFileRepository(usersFile.resolveSibling(PASSES_FILE_NAME)), tickets);
            ParkController.initialize(new ParkServices(users, groups, tickets, attractions, passes), new InvoiceDirector());
            HelpGenerator help = new HelpGenerator(CommandCatalog.standard(), new SyntaxHelpFormatter(),
                    new ExtendedHelpFormatter());
            runner = new CommandRunner(new CommandProcessor(ParkController.getInstance(), help));
        } catch (IOException e) {
            out.println("ERROR: Could not load stored data next to '" + usersFile + "': " + e);
            return 1;
        }
        try {
            return runCommands(args, stdin, out, runner);
        } finally {
            ParkController.shutdown();
        }
    }

    private static int runCommands(String[] args, InputStream stdin, PrintStream out, CommandRunner runner) {
        if (args.length == 0) {
            BufferedReader in = new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8));
            return runner.run(in, out) ? 0 : 1;
        }
        String file = args[0];
        try (BufferedReader in = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
            return runner.run(in, out) ? 0 : 1;
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR: Cannot open command file '" + file + "': " + e);
            return 1;
        }
    }

    /** Creates tmp/yyyyMMdd_HHmmss (or with a _n suffix if taken). Technical timestamp, not a business date. */
    private static Path createFlyDirectory(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String stamp = LocalDateTime.now(ZoneId.systemDefault()).format(RUN_STAMP);
        Path dir = tmpRoot.resolve(stamp);
        for (int n = 1; Files.exists(dir); n++) {
            dir = tmpRoot.resolve(stamp + "_" + n);
        }
        return Files.createDirectory(dir);
    }

    /** Deletes the directory tree; returns false if something could not be deleted. */
    private static boolean deleteQuietly(Path dir) {
        if (!Files.exists(dir)) {
            return true;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void removeHook(Thread hook) {
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException e) {
            // the JVM is already shutting down; the hook will run and find nothing to delete
        }
    }
}
