package es.upm.parque;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.MainController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.StandardInvoiceBuilder;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;
import es.upm.parque.presentation.CommandProcessor;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Builds the full application object graph over the data files of a test directory.
 * Creating a new instance over the same directory simulates restarting the application.
 */
public final class TestPark {

    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);

    public final MainController controller;
    public final UserController users;
    public final GroupController groups;
    public final TicketController tickets;
    public final AttractionController attractions;
    public final InvoiceController invoices;
    public final CommandProcessor processor;

    /**
     * Configures the singleton controllers over the data files of {@code dir}, exactly as the
     * application does at startup.
     */
    public TestPark(Path dir) {
        processor = App.configure(dir, CLOCK);
        controller = MainController.getInstance();
        users = UserController.getInstance();
        groups = GroupController.getInstance();
        tickets = TicketController.getInstance();
        attractions = AttractionController.getInstance();
        invoices = InvoiceController.getInstance();
    }

    public App app() {
        return new App(processor);
    }
}
