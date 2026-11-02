package com.amusementpark.presentation;

import com.amusementpark.business.controller.AttractionController;
import com.amusementpark.business.controller.GroupController;
import com.amusementpark.business.controller.InvoiceController;
import com.amusementpark.business.controller.ParkController;
import com.amusementpark.business.controller.TicketController;
import com.amusementpark.business.controller.UserController;
import com.amusementpark.business.invoice.TextInvoiceBuilder;
import com.amusementpark.business.security.BCryptPasswordHasher;
import com.amusementpark.business.security.PasswordHasher;
import com.amusementpark.persistence.AttractionFileRepository;
import com.amusementpark.persistence.GroupFileRepository;
import com.amusementpark.persistence.StoragePaths;
import com.amusementpark.persistence.TemporaryStorage;
import com.amusementpark.persistence.TicketFileRepository;
import com.amusementpark.persistence.UserFileRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Application entry point. Without a command file, commands are read from standard input;
 * otherwise from the given file. A leading {@code --fly} keeps all persistence in a temporary
 * directory that is deleted when the run ends.
 */
public class App {

    static final String FLY_FLAG = "--fly";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, Path.of(""));
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application with the given streams; persistence files live in {@code workDir}
     * (normal mode) or in a new {@code workDir/tmp/<timestamp>} directory ({@code --fly}).
     *
     * @return 0 on normal termination, 1 if data or the command file could not be read
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path workDir) {
        boolean fly = args.length > 0 && FLY_FLAG.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;
        if (!fly) {
            return runWith(StoragePaths.in(workDir), commandFile, in, out, err);
        }
        Path flyDir;
        try {
            flyDir = TemporaryStorage.create(workDir.resolve("tmp"));
        } catch (IOException e) {
            err.println("ERROR: Could not create temporary directory: " + e.getMessage());
            return 1;
        }
        Thread cleanup = new Thread(() -> TemporaryStorage.delete(flyDir));
        Runtime.getRuntime().addShutdownHook(cleanup);
        try {
            return runWith(StoragePaths.in(flyDir), commandFile, in, out, err);
        } finally {
            TemporaryStorage.delete(flyDir);
            try {
                Runtime.getRuntime().removeShutdownHook(cleanup);
            } catch (IllegalStateException e) {
                // JVM already shutting down: the hook runs anyway.
            }
        }
    }

    private static int runWith(StoragePaths paths, String commandFile, InputStream in, PrintStream out,
                               PrintStream err) {
        ParkController park;
        try {
            park = configure(paths, new BCryptPasswordHasher(), LocalDate.now());
        } catch (IOException e) {
            err.println("ERROR: Could not read data files: " + e.getMessage());
            return 1;
        }
        CommandProcessor processor = createProcessor(park, out);
        if (commandFile == null) {
            return runInteractive(processor, in, err);
        }
        return runFile(processor, commandFile, err);
    }

    /**
     * Composition root: creates the repositories for the given paths and injects them, and the
     * collaborating controllers, into the Singleton controllers for a new execution.
     */
    public static ParkController configure(StoragePaths paths, PasswordHasher hasher, LocalDate startDate)
            throws IOException {
        UserController users = UserController.getInstance();
        users.configure(new UserFileRepository(paths.users()), hasher);
        GroupController groups = GroupController.getInstance();
        groups.configure(new GroupFileRepository(paths.groups()), users);
        TicketController tickets = TicketController.getInstance();
        tickets.configure(new TicketFileRepository(paths.tickets(), paths.groupTickets()), users, groups);
        AttractionController attractions = AttractionController.getInstance();
        attractions.configure(new AttractionFileRepository(paths.attractions()));
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(tickets, TextInvoiceBuilder::new);
        ParkController park = ParkController.getInstance();
        park.configure(users, groups, tickets, attractions, invoices, startDate);
        return park;
    }

    /** Creates the command processor with the concise and extended help generators injected. */
    public static CommandProcessor createProcessor(ParkController park, PrintStream out) {
        return new CommandProcessor(park, new HelpGenerator(new SyntaxHelpFormatter()),
                new HelpGenerator(new ExtendedHelpFormatter()), out);
    }

    private static int runInteractive(CommandProcessor processor, InputStream in, PrintStream err) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            processor.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: Could not read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandProcessor processor, String fileName, PrintStream err) {
        Path path;
        try {
            path = Path.of(fileName);
        } catch (InvalidPathException e) {
            err.println("ERROR: Invalid command file path: " + fileName);
            return 1;
        }
        if (!Files.isRegularFile(path)) {
            err.println("ERROR: Command file not found: " + fileName);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            processor.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: Could not read command file " + fileName + ": " + e.getMessage());
            return 1;
        }
    }
}
