package es.upm.amusementpark.presentation;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.model.SimpleInvoiceBuilder;
import es.upm.amusementpark.persistence.AttractionFileRepository;
import es.upm.amusementpark.persistence.GroupFileRepository;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.persistence.TicketFileRepository;
import es.upm.amusementpark.persistence.UserFileRepository;

/**
 * Application start-up: creates the repositories for the chosen storage location
 * and initializes the singleton controllers, injecting their dependencies.
 * {@link #shutdown()} ends the execution so a new one can start.
 */
public final class AppBootstrap {

    private AppBootstrap() {
    }

    public static ParkController start(StoragePaths paths) throws IOException {
        return start(paths, LocalDate.now());
    }

    public static ParkController start(StoragePaths paths, LocalDate startDate) throws IOException {
        try {
            UserController users = UserController.initialize(new UserFileRepository(paths.users()));
            GroupController groups = GroupController.initialize(new GroupFileRepository(paths.groups()), users);
            TicketController tickets = TicketController.initialize(
                    new TicketFileRepository(paths.tickets()), users, groups);
            AttractionController attractions = AttractionController.initialize(
                    new AttractionFileRepository(paths.attractions()));
            InvoiceController invoices = InvoiceController.initialize(tickets, SimpleInvoiceBuilder::new);
            return ParkController.initialize(users, groups, tickets, attractions, invoices, startDate);
        } catch (IOException | RuntimeException e) {
            shutdown();
            throw e;
        }
    }

    /**
     * Help generation with the formatter used for each HELP mode.
     */
    public static HelpGenerator help() {
        return new HelpGenerator(CommandRegistry.commands(), Map.of(
                "", new ConciseHelpFormatter(),
                "EXTENDED", new ExtendedHelpFormatter()));
    }

    public static void shutdown() {
        ParkController.reset();
        InvoiceController.reset();
        AttractionController.reset();
        TicketController.reset();
        GroupController.reset();
        UserController.reset();
    }
}
