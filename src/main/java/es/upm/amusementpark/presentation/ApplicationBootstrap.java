package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.invoice.TextInvoiceBuilder;
import es.upm.amusementpark.persistence.FileAttractionRepository;
import es.upm.amusementpark.persistence.FileGroupRepository;
import es.upm.amusementpark.persistence.FileTicketRepository;
import es.upm.amusementpark.persistence.FileUserRepository;
import es.upm.amusementpark.persistence.StorageLocation;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Composition root: creates the repositories for the active storage location (normal or --fly) and injects
 * them, together with the collaborating controllers, into the unique controllers.
 */
public final class ApplicationBootstrap {

    private ApplicationBootstrap() {
    }

    public static CommandProcessor start(StorageLocation storage, PrintStream out) throws IOException {
        UserController users = UserController.getInstance();
        users.configure(new FileUserRepository(storage.usersFile()));

        GroupController groups = GroupController.getInstance();
        groups.configure(new FileGroupRepository(storage.groupsFile()), users);

        TicketController tickets = TicketController.getInstance();
        tickets.configure(new FileTicketRepository(storage.ticketsFile(), storage.groupTicketsFile()), users, groups);

        AttractionController attractions = AttractionController.getInstance();
        attractions.configure(new FileAttractionRepository(storage.attractionsFile()));

        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(tickets, TextInvoiceBuilder::new);

        ParkController park = ParkController.getInstance();
        park.configure(users, groups, tickets, attractions, invoices);

        return new CommandProcessor(park, out);
    }
}
