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
import es.upm.parque.business.model.SystemDate;
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
            processor = Bootstrap.configure(dir, today);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        date = processor.getSystemDate();
        users = UserManager.getInstance();
        groups = GroupManager.getInstance();
        tickets = TicketManager.getInstance();
        attractions = AttractionManager.getInstance();
        invoices = InvoiceManager.getInstance();
    }

    public String run(String command) {
        return processor.process(command);
    }
}
