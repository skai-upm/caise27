package es.upm.amusementpark.presentation;

import java.io.IOException;
import java.time.LocalDate;

import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.SystemController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.invoice.StandardInvoiceBuilder;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.FileAttractionRepository;
import es.upm.amusementpark.persistence.FileGroupRepository;
import es.upm.amusementpark.persistence.FileTicketRepository;
import es.upm.amusementpark.persistence.FileUserRepository;
import es.upm.amusementpark.persistence.StorageConfig;
import es.upm.amusementpark.presentation.help.CommandCatalog;
import es.upm.amusementpark.presentation.help.ConciseHelpFormatter;
import es.upm.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.amusementpark.presentation.help.HelpGenerator;

/**
 * Composition root of one execution: creates the repositories for the chosen
 * storage, initializes the controllers with their dependencies, and builds the
 * command processor. {@link #stop()} ends the execution's controller instances.
 */
public record ApplicationContext(SystemController system, UserController users, ParkController park,
                                 InvoiceController invoices, CommandProcessor processor) {

    public static ApplicationContext start(StorageConfig storage, LocalDate startDate) throws IOException {
        return start(storage, startDate, new BCryptPasswordHasher());
    }

    public static ApplicationContext start(StorageConfig storage, LocalDate startDate, PasswordHasher hasher)
            throws IOException {
        try {
            SystemController system = SystemController.initialize(startDate);
            UserController users = UserController.initialize(new FileUserRepository(storage.users()), hasher);
            ParkController park = ParkController.initialize(system, users,
                    new FileGroupRepository(storage.groups()),
                    new FileTicketRepository(storage.tickets()),
                    new FileAttractionRepository(storage.attractions()));
            InvoiceController invoices = InvoiceController.initialize(users, park, StandardInvoiceBuilder::new);
            HelpGenerator help = new HelpGenerator(CommandCatalog.COMMANDS, new ConciseHelpFormatter());
            HelpGenerator extendedHelp = new HelpGenerator(CommandCatalog.COMMANDS, new ExtendedHelpFormatter());
            return new ApplicationContext(system, users, park, invoices,
                    new CommandProcessor(system, users, park, invoices, help, extendedHelp));
        } catch (IOException | RuntimeException e) {
            stop();
            throw e;
        }
    }

    public static void stop() {
        InvoiceController.release();
        ParkController.release();
        UserController.release();
        SystemController.release();
    }
}
