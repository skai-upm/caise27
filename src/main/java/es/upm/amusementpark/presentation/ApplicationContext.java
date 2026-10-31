package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.AttractionManager;
import es.upm.amusementpark.business.controller.GroupManager;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.TicketManager;
import es.upm.amusementpark.business.controller.UserManager;
import es.upm.amusementpark.business.invoice.InvoiceService;
import es.upm.amusementpark.business.invoice.StandardInvoiceBuilder;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.FileAttractionRepository;
import es.upm.amusementpark.persistence.FileGroupRepository;
import es.upm.amusementpark.persistence.FileTicketRepository;
import es.upm.amusementpark.persistence.FileUserRepository;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.presentation.help.CommandRegistry;
import es.upm.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.amusementpark.presentation.help.HelpGenerator;
import es.upm.amusementpark.presentation.help.SyntaxHelpFormatter;

import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;

/**
 * Composition root: creates the repositories for the chosen storage
 * location, initialises the single controller instances of an execution and
 * injects their dependencies. Normal and {@code --fly} runs differ only in
 * the {@link StoragePaths} given here.
 */
public final class ApplicationContext {

    private ApplicationContext() {
    }

    /** Wires and initialises every controller for one execution with BCrypt password protection. */
    public static ParkController start(StoragePaths paths, LocalDate today) throws IOException {
        return start(paths, today, new BCryptPasswordHasher());
    }

    /** Wires and initialises every controller for one execution with the given password protection. */
    public static ParkController start(StoragePaths paths, LocalDate today, PasswordHasher hasher) throws IOException {
        try {
            UserManager users = UserManager.initialize(new FileUserRepository(paths.users()), hasher);
            GroupManager groups = GroupManager.initialize(new FileGroupRepository(paths.groups()), users);
            TicketManager tickets = TicketManager.initialize(new FileTicketRepository(paths), users, groups);
            AttractionManager attractions =
                    AttractionManager.initialize(new FileAttractionRepository(paths.attractions()));
            return ParkController.initialize(users, groups, tickets, attractions,
                    new InvoiceService(StandardInvoiceBuilder::new), today);
        } catch (IOException | RuntimeException e) {
            stop();
            throw e;
        }
    }

    /** Creates the command processor with its help generators and output format behaviors injected. */
    public static CommandProcessor commandProcessor(ParkController park, PrintStream out) {
        CommandRegistry registry = CommandRegistry.standard();
        return new CommandProcessor(park, out,
                new HelpGenerator(registry, new SyntaxHelpFormatter()),
                new HelpGenerator(registry, new ExtendedHelpFormatter()));
    }

    /** Ends the execution's controller instances. */
    public static void stop() {
        ParkController.reset();
        AttractionManager.reset();
        TicketManager.reset();
        GroupManager.reset();
        UserManager.reset();
    }
}
