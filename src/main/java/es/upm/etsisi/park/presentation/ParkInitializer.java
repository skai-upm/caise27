package es.upm.etsisi.park.presentation;

import es.upm.etsisi.park.business.controller.AttractionController;
import es.upm.etsisi.park.business.controller.GroupController;
import es.upm.etsisi.park.business.controller.InvoiceController;
import es.upm.etsisi.park.business.controller.ParkController;
import es.upm.etsisi.park.business.controller.PassController;
import es.upm.etsisi.park.business.controller.SystemController;
import es.upm.etsisi.park.business.controller.TicketController;
import es.upm.etsisi.park.business.controller.UserController;
import es.upm.etsisi.park.business.security.PasswordHasher;
import es.upm.etsisi.park.persistence.ParkStorage;
import es.upm.etsisi.park.presentation.help.CommandCatalog;
import es.upm.etsisi.park.presentation.help.ExtendedHelpFormatter;
import es.upm.etsisi.park.presentation.help.HelpGenerator;
import es.upm.etsisi.park.presentation.help.SyntaxHelpFormatter;
import java.time.LocalDate;

/**
 * Composition root: creates and wires every controller with the repositories and services it needs.
 * Normal and --fly execution only differ in the {@link ParkStorage} passed here.
 */
public final class ParkInitializer {

    private ParkInitializer() {
    }

    public static ParkController initialize(ParkStorage storage, PasswordHasher hasher, LocalDate startDate) {
        SystemController system = SystemController.getInstance();
        system.initialize(startDate);

        UserController users = UserController.getInstance();
        users.initialize(storage.users(), hasher);

        GroupController groups = GroupController.getInstance();
        groups.initialize(storage.groups(), users);

        TicketController tickets = TicketController.getInstance();
        tickets.initialize(storage.tickets(), system, users, groups::find);

        PassController passes = PassController.getInstance();
        passes.initialize(storage.passes(), system, tickets);

        AttractionController attractions = AttractionController.getInstance();
        attractions.initialize(storage.attractions(), system, tickets, passes);

        InvoiceController invoices = InvoiceController.getInstance();
        invoices.initialize(system, users, groups, tickets);

        ParkController park = ParkController.getInstance();
        park.initialize(system, users, groups, tickets, passes, attractions, invoices);
        return park;
    }

    public static CommandProcessor commandProcessor(ParkController park) {
        CommandCatalog catalog = new CommandCatalog();
        return new CommandProcessor(park,
                new HelpGenerator(catalog, new SyntaxHelpFormatter()),
                new HelpGenerator(catalog, new ExtendedHelpFormatter()));
    }
}
