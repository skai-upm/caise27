package es.upm.parque;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.business.controller.AttractionManager;
import es.upm.parque.business.controller.GroupManager;
import es.upm.parque.business.controller.InvoiceManager;
import es.upm.parque.business.controller.TicketManager;
import es.upm.parque.business.controller.UserManager;
import es.upm.parque.business.model.StandardInvoiceBuilder;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.persistence.TextFileAttractionRepository;
import es.upm.parque.persistence.TextFileGroupRepository;
import es.upm.parque.persistence.TextFileGroupTicketRepository;
import es.upm.parque.persistence.TextFileTicketRepository;
import es.upm.parque.persistence.TextFileUserRepository;
import es.upm.parque.presentation.CommandProcessor;

/**
 * Startup configuration: creates the repositories for a data directory and initialises the
 * singleton controllers, injecting into each one the repositories, date and controllers it needs.
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    public static CommandProcessor configure(Path dataDir, LocalDate today) throws IOException {
        SystemDate systemDate = new SystemDate(today);
        UserManager users = UserManager.initialize(new TextFileUserRepository(dataDir.resolve("users.txt")));
        GroupManager groups = GroupManager.initialize(
                new TextFileGroupRepository(dataDir.resolve("groups.txt")), users);
        TicketManager tickets = TicketManager.initialize(
                new TextFileTicketRepository(dataDir.resolve("tickets.txt")),
                new TextFileGroupTicketRepository(dataDir.resolve("grouptickets.txt")), users, groups, systemDate);
        AttractionManager attractions = AttractionManager.initialize(
                new TextFileAttractionRepository(dataDir.resolve("attractions.txt")), tickets, systemDate);
        InvoiceManager invoices = InvoiceManager.initialize(users, groups, tickets, systemDate,
                StandardInvoiceBuilder::new);
        return new CommandProcessor(users, groups, tickets, attractions, invoices, systemDate);
    }
}
