package es.upm.parque;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.StandardInvoiceBuilder;
import es.upm.parque.business.security.BCryptPasswordHasher;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.persistence.TextAttractionRepository;
import es.upm.parque.persistence.TextGroupRepository;
import es.upm.parque.persistence.TextPassRepository;
import es.upm.parque.persistence.TextTicketRepository;
import es.upm.parque.persistence.TextUserRepository;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.CommandRegistry;
import es.upm.parque.presentation.command.AttractionCommands;
import es.upm.parque.presentation.command.GroupCommands;
import es.upm.parque.presentation.command.HelpCommands;
import es.upm.parque.presentation.command.InvoiceCommands;
import es.upm.parque.presentation.command.PassCommands;
import es.upm.parque.presentation.command.SystemCommands;
import es.upm.parque.presentation.command.TicketCommands;
import es.upm.parque.presentation.command.UserCommands;
import es.upm.parque.presentation.help.ConciseHelpFormatter;
import es.upm.parque.presentation.help.ExtendedHelpFormatter;
import es.upm.parque.presentation.help.HelpGenerator;

import java.nio.file.Path;

/**
 * Composition root. Creates the repositories for a data directory, injects them and the
 * controller collaborations into the Singleton controllers, loads the stored state and registers
 * the commands. Normal and --fly executions differ only in the directory passed here.
 */
public final class ParkApplication {

    private ParkApplication() {
    }

    public static CommandProcessor start(Path dataDirectory) {
        return start(dataDirectory, new BCryptPasswordHasher());
    }

    public static CommandProcessor start(Path dataDirectory, PasswordHasher hasher) {
        ParkController park = ParkController.getInstance();
        UserController users = UserController.getInstance();
        GroupController groups = GroupController.getInstance();
        TicketController tickets = TicketController.getInstance();
        PassController passes = PassController.getInstance();
        AttractionController attractions = AttractionController.getInstance();
        InvoiceController invoices = InvoiceController.getInstance();

        park.reset();
        users.configure(new TextUserRepository(dataDirectory.resolve(TextUserRepository.FILE_NAME)),
                hasher, park, tickets, groups);
        groups.configure(new TextGroupRepository(dataDirectory.resolve(TextGroupRepository.FILE_NAME)),
                users, tickets, park);
        tickets.configure(new TextTicketRepository(
                        dataDirectory.resolve(TextTicketRepository.TICKETS_FILE_NAME),
                        dataDirectory.resolve(TextTicketRepository.GROUP_TICKETS_FILE_NAME)),
                users, groups, passes, park);
        passes.configure(new TextPassRepository(dataDirectory.resolve(TextPassRepository.FILE_NAME)), tickets, park);
        attractions.configure(new TextAttractionRepository(dataDirectory.resolve(TextAttractionRepository.FILE_NAME)),
                tickets, passes, users, park);
        invoices.configure(users, groups, tickets, passes, StandardInvoiceBuilder::new);

        users.load();
        groups.load();
        tickets.load();
        passes.load();
        attractions.load();

        CommandRegistry registry = new CommandRegistry();
        UserCommands.register(registry, users);
        SystemCommands.register(registry, park);
        GroupCommands.register(registry, groups);
        TicketCommands.register(registry, tickets);
        PassCommands.register(registry, passes);
        AttractionCommands.register(registry, attractions);
        InvoiceCommands.register(registry, invoices);
        HelpCommands.register(registry, new HelpGenerator(new ConciseHelpFormatter()),
                new HelpGenerator(new ExtendedHelpFormatter()));
        return new CommandProcessor(registry);
    }
}
