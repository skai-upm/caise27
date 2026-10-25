package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.business.controller.AttractionManager;
import es.upm.parque.business.controller.GroupManager;
import es.upm.parque.business.controller.InvoiceManager;
import es.upm.parque.business.controller.TicketManager;
import es.upm.parque.business.controller.UserManager;
import es.upm.parque.business.model.StandardInvoiceBuilder;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.GroupTicketRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;
import es.upm.parque.presentation.CommandProcessor;

/**
 * Test fixture: the whole application wired over files in one directory.
 */
public final class Park {

    public final SystemDate date;
    public final UserManager users;
    public final GroupManager groups;
    public final TicketManager tickets;
    public final AttractionManager attractions;
    public final InvoiceManager invoices;
    public final CommandProcessor processor;

    public Park(Path dir, LocalDate today) {
        try {
            date = new SystemDate(today);
            users = new UserManager(new UserRepository(dir.resolve("users.txt")));
            groups = new GroupManager(new GroupRepository(dir.resolve("groups.txt")), users);
            tickets = new TicketManager(new TicketRepository(dir.resolve("tickets.txt")),
                    new GroupTicketRepository(dir.resolve("grouptickets.txt")), users, groups, date);
            attractions = new AttractionManager(new AttractionRepository(dir.resolve("attractions.txt")), tickets,
                    date);
            invoices = new InvoiceManager(users, groups, tickets, date, StandardInvoiceBuilder::new);
            processor = new CommandProcessor(users, groups, tickets, attractions, invoices, date);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String run(String command) {
        return processor.process(command);
    }
}
