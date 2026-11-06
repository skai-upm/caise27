package es.upm.park;

import es.upm.park.business.controller.InvoiceController;
import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.UserController;
import es.upm.park.business.controller.UserRegistry;
import es.upm.park.business.security.BCryptPasswordHasher;
import es.upm.park.business.security.PasswordService;
import es.upm.park.business.validation.UserValidator;
import es.upm.park.persistence.AttractionRepository;
import es.upm.park.persistence.GroupRepository;
import es.upm.park.persistence.PassRepository;
import es.upm.park.persistence.StoragePaths;
import es.upm.park.persistence.TextParkStorage;
import es.upm.park.persistence.TicketRepository;
import es.upm.park.persistence.UserRepository;
import es.upm.park.presentation.CommandProcessor;
import es.upm.park.presentation.help.CommandCatalog;
import es.upm.park.presentation.help.ExtendedHelpFormatter;
import es.upm.park.presentation.help.HelpGenerator;
import es.upm.park.presentation.help.SyntaxHelpFormatter;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Hand-written dependency injection: builds the repositories for a data
 * directory, injects them into the storage and controllers, wires controller
 * collaborations and creates the presentation entry point.
 */
public final class AppConfiguration {

    /** The configured objects of one execution. */
    public record Configured(TextParkStorage storage, GroupRepository groups, TicketRepository tickets,
                             AttractionRepository attractions, PasswordService passwords, UserRegistry users,
                             UserRepository userStorage, CommandProcessor processor) {
    }

    private AppConfiguration() {
    }

    public static Configured configure(Path dataDir, PrintStream warnings) {
        StoragePaths paths = new StoragePaths(dataDir);
        GroupRepository groups = new GroupRepository(paths.groups(), warnings);
        TicketRepository tickets = new TicketRepository(paths.tickets(), warnings);
        AttractionRepository attractions = new AttractionRepository(paths.attractions(), warnings);
        PassRepository passes = new PassRepository(paths.passes(), warnings);
        TextParkStorage storage = new TextParkStorage(groups, tickets, attractions, passes);

        UserRegistry users = new UserRegistry();
        ParkController park = ParkController.getInstance();
        park.setStorage(storage);
        park.setUserDirectory(users);
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.setParkController(park);

        PasswordService passwords = new PasswordService(new BCryptPasswordHasher(), UserValidator.passwordPolicy());

        UserController userController = UserController.getInstance();
        UserRepository userStorage = new UserRepository(paths.users(), warnings);
        userController.configure(users, passwords, userStorage);
        userController.setParkController(park);

        HelpGenerator help = new HelpGenerator(CommandCatalog.standard(), new SyntaxHelpFormatter());
        HelpGenerator extendedHelp = new HelpGenerator(CommandCatalog.standard(), new ExtendedHelpFormatter());

        return new Configured(storage, groups, tickets, attractions, passwords, users, userStorage,
                new CommandProcessor(park, invoices, userController, help, extendedHelp));
    }
}
