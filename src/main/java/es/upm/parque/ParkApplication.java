package es.upm.parque;

import java.io.IOException;
import java.time.LocalDate;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.InvoiceDirector;
import es.upm.parque.business.invoice.StandardInvoiceBuilder;
import es.upm.parque.business.security.PasswordEncoder;
import es.upm.parque.persistence.FileAttractionRepository;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FileTicketRepository;
import es.upm.parque.persistence.FileUserRepository;
import es.upm.parque.persistence.StoragePaths;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.ParkCommands;
import es.upm.parque.presentation.command.CommandRegistry;
import es.upm.parque.presentation.help.ExtendedHelpFormatter;
import es.upm.parque.presentation.help.HelpGenerator;
import es.upm.parque.presentation.help.SyntaxHelpFormatter;

/**
 * Composition root: creates the repositories for the given storage paths, injects them and the collaborating
 * controllers into the Singleton controllers, loads the stored data and registers the commands.
 */
public final class ParkApplication {

    private ParkApplication() {
    }

    public static CommandProcessor start(StoragePaths paths, LocalDate startDate, PasswordEncoder passwordEncoder)
            throws IOException {
        ParkController park = ParkController.getInstance();
        UserController users = UserController.getInstance();
        GroupController groups = GroupController.getInstance();
        TicketController tickets = TicketController.getInstance();
        PassController passes = PassController.getInstance();
        AttractionController attractions = AttractionController.getInstance();
        InvoiceController invoices = InvoiceController.getInstance();

        park.configure(startDate);
        users.configure(new FileUserRepository(paths.users()), passwordEncoder, park, tickets, groups);
        groups.configure(new FileGroupRepository(paths.groups()), park, users, tickets);
        tickets.configure(new FileTicketRepository(paths.tickets(), paths.groupTickets(), paths.passes()), park,
                users, groups);
        passes.configure(park, tickets);
        attractions.configure(new FileAttractionRepository(paths.attractions()), park, tickets);
        invoices.configure(park, users, groups, tickets, new InvoiceDirector(), StandardInvoiceBuilder::new);

        users.load();
        groups.load();
        tickets.load();
        attractions.load();

        CommandRegistry registry = new CommandRegistry();
        CommandProcessor processor = new CommandProcessor(registry);
        ParkCommands.register(registry, processor,
                new ParkCommands.Controllers(park, users, groups, tickets, passes, attractions, invoices),
                new HelpGenerator(registry, new SyntaxHelpFormatter()),
                new HelpGenerator(registry, new ExtendedHelpFormatter()));
        return processor;
    }
}
