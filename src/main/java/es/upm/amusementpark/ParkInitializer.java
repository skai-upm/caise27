package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.controller.SystemController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.repository.Repositories;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.InvoicePrinter;
import es.upm.amusementpark.presentation.help.CommandCatalog;
import es.upm.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.amusementpark.presentation.help.HelpGenerator;
import es.upm.amusementpark.presentation.help.SyntaxHelpFormatter;

import java.io.PrintStream;
import java.time.LocalDate;

/**
 * Composition root: the only place that wires the singleton controllers with the repositories and
 * collaborators they depend on (hand-written dependency injection), and assembles the presentation.
 */
public final class ParkInitializer {

    private ParkInitializer() {
    }

    /**
     * Configures every controller with the given repositories, hasher and start date, then loads the
     * stored park state.
     */
    public static Park initialize(Repositories repositories, PasswordHasher hasher, LocalDate today)
            throws PersistenceException {
        SystemController system = SystemController.getInstance();
        UserController users = UserController.getInstance();
        GroupController groups = GroupController.getInstance();
        TicketController tickets = TicketController.getInstance();
        PassController passes = PassController.getInstance();
        AttractionController attractions = AttractionController.getInstance();
        InvoiceController invoices = InvoiceController.getInstance();

        system.reset(today);
        users.configure(repositories.users(), hasher);
        users.setCollaborators(tickets, groups, attractions);
        groups.configure(repositories.groups());
        groups.setCollaborators(users, tickets);
        tickets.configure(repositories.tickets());
        tickets.setCollaborators(system, users, groups, passes);
        passes.configure(repositories.passes());
        passes.setCollaborators(system, tickets);
        attractions.configure(repositories.attractions());
        attractions.setCollaborators(system, users, tickets, passes);
        invoices.setCollaborators(users, groups, tickets, passes);

        Park park = Park.getInstance();
        park.setControllers(system, users, groups, tickets, passes, attractions, invoices);
        park.load();
        return park;
    }

    /**
     * Command processor with basic and extended help, each receiving its formatting strategy.
     */
    public static CommandProcessor commandProcessor(Park park, PrintStream out) {
        CommandCatalog catalog = new CommandCatalog();
        return new CommandProcessor(park,
                new HelpGenerator(catalog, new SyntaxHelpFormatter()),
                new HelpGenerator(catalog, new ExtendedHelpFormatter()),
                new InvoicePrinter(), out);
    }
}
