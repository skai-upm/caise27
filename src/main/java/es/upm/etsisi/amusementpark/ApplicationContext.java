package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.attraction.AttractionController;
import es.upm.etsisi.amusementpark.business.group.GroupController;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceController;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.park.ParkRepository;
import es.upm.etsisi.amusementpark.business.pass.PassController;
import es.upm.etsisi.amusementpark.business.ticket.TicketController;
import es.upm.etsisi.amusementpark.business.user.PasswordHasher;
import es.upm.etsisi.amusementpark.business.user.UserController;
import es.upm.etsisi.amusementpark.presentation.AttractionCommands;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.CommandRegistry;
import es.upm.etsisi.amusementpark.presentation.ControlCommands;
import es.upm.etsisi.amusementpark.presentation.InvoiceCommands;
import es.upm.etsisi.amusementpark.presentation.InvoiceFormatter;
import es.upm.etsisi.amusementpark.presentation.ParkCommands;
import es.upm.etsisi.amusementpark.presentation.UserCommands;
import es.upm.etsisi.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;
import es.upm.etsisi.amusementpark.presentation.help.SyntaxHelpFormatter;
import java.time.LocalDate;

/**
 * Composition root: configures the singleton controllers with the injected repository and
 * collaborators, and builds the presentation layer on top of them. Hand-written dependency
 * injection; no framework.
 */
public final class ApplicationContext {

    private ApplicationContext() {
    }

    public static CommandProcessor start(ParkRepository repository, LocalDate today, PasswordHasher hasher)
            throws StorageException {
        Park park = Park.getInstance();
        park.configure(repository, today);

        GroupController groups = GroupController.getInstance();
        groups.configure(park);
        PassController passes = PassController.getInstance();
        passes.configure(park);
        TicketController tickets = TicketController.getInstance();
        tickets.configure(park, passes);
        UserController users = UserController.getInstance();
        users.configure(park, hasher, tickets, groups);
        AttractionController attractions = AttractionController.getInstance();
        attractions.configure(park, passes);
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(park);

        CommandRegistry registry = new CommandRegistry();
        UserCommands.register(registry, users);
        ParkCommands.register(registry, park, groups, tickets, passes);
        AttractionCommands.register(registry, attractions);
        InvoiceCommands.register(registry, invoices, new InvoiceFormatter());
        ControlCommands.register(registry, new HelpGenerator(registry, new SyntaxHelpFormatter()),
                new HelpGenerator(registry, new ExtendedHelpFormatter()));
        return new CommandProcessor(registry);
    }
}
