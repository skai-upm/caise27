package es.upm.parque.presentation;

import es.upm.parque.business.controller.AttractionManager;
import es.upm.parque.business.controller.GroupManager;
import es.upm.parque.business.controller.InvoiceDirector;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.TicketManager;
import es.upm.parque.business.controller.UserManager;
import es.upm.parque.persistence.AttractionStorage;
import es.upm.parque.persistence.DataDirectory;
import es.upm.parque.persistence.GroupStorage;
import es.upm.parque.persistence.TicketStorage;
import es.upm.parque.persistence.UserStorage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Starts the application. Without a file argument commands are read from standard input; otherwise the
 * first argument that is not {@code --fly} is the path of a file with one command per line. Both sources
 * share the same processing. With {@code --fly} all persistence files live in a temporary directory that is
 * deleted when the application ends.
 */
public class App {

    static final String FLY_FLAG = "--fly";
    static final Path DATA_DIR = Path.of("data");
    static final Path TMP_DIR = Path.of("tmp");
    static final String USERS_FILE_NAME = "users.txt";
    static final String GROUPS_FILE_NAME = "groups.txt";
    static final String TICKETS_FILE_NAME = "tickets.txt";
    static final String GROUP_TICKETS_FILE_NAME = "group_tickets.txt";
    static final String ATTRACTIONS_FILE_NAME = "attractions.txt";

    public static void main(String[] args) {
        run(args, System.in, System.out, DATA_DIR, TMP_DIR);
    }

    static void run(String[] args, InputStream stdin, PrintStream out, Path dataDir, Path tmpBase) {
        boolean fly = false;
        String commandFile = null;
        for (String arg : args) {
            if (arg.equals(FLY_FLAG)) {
                fly = true;
            } else if (commandFile == null) {
                commandFile = arg;
            }
        }
        DataDirectory data;
        try {
            data = fly ? DataDirectory.temporary(tmpBase, LocalDateTime.now()) : DataDirectory.permanent(dataDir);
        } catch (UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
            return;
        }
        Thread cleanup = new Thread(data::close);
        Runtime.getRuntime().addShutdownHook(cleanup);
        try (data) {
            CommandProcessor processor = new CommandProcessor(configure(data));
            if (commandFile == null) {
                runSource(processor, new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out,
                    "standard input");
            } else {
                runFile(processor, Path.of(commandFile), out);
            }
        } catch (UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
        } finally {
            Runtime.getRuntime().removeShutdownHook(cleanup);
        }
    }

    /**
     * Startup configuration: creates the file repositories of {@code data} and injects them, and the
     * collaborations between controllers, into the single controller instances.
     */
    static ParkController configure(DataDirectory data) {
        UserManager users = UserManager.getInstance().configure(new UserStorage(data.resolve(USERS_FILE_NAME)));
        GroupManager groups = GroupManager.getInstance()
            .configure(users, new GroupStorage(data.resolve(GROUPS_FILE_NAME)));
        TicketManager tickets = TicketManager.getInstance().configure(users, groups,
            new TicketStorage(data.resolve(TICKETS_FILE_NAME), data.resolve(GROUP_TICKETS_FILE_NAME)));
        AttractionManager attractions = AttractionManager.getInstance()
            .configure(new AttractionStorage(data.resolve(ATTRACTIONS_FILE_NAME)));
        return ParkController.getInstance()
            .configure(users, groups, tickets, attractions, new InvoiceDirector(), LocalDate.now());
    }

    private static void runFile(CommandProcessor processor, Path commandFile, PrintStream out) {
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runSource(processor, reader, out, "command file " + commandFile);
        } catch (IOException e) {
            out.println("ERROR: Cannot open command file " + commandFile + " (" + describe(e) + ")");
        }
    }

    private static void runSource(CommandProcessor processor, BufferedReader reader, PrintStream out, String name) {
        try {
            processor.run(reader, out);
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: Cannot read " + name + " (" + describe(e) + ")");
        }
    }

    private static String describe(Exception e) {
        return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
    }
}
