package es.upm.etsisi.park;

import es.upm.etsisi.park.business.AttractionController;
import es.upm.etsisi.park.business.GroupController;
import es.upm.etsisi.park.business.InvoiceController;
import es.upm.etsisi.park.business.Park;
import es.upm.etsisi.park.business.ParkRepository;
import es.upm.etsisi.park.business.ParkServices;
import es.upm.etsisi.park.business.PassController;
import es.upm.etsisi.park.business.PasswordHasher;
import es.upm.etsisi.park.business.TicketController;
import es.upm.etsisi.park.business.UserController;
import es.upm.etsisi.park.presentation.CommandProcessor;
import es.upm.etsisi.park.presentation.ExtendedHelpFormatter;
import es.upm.etsisi.park.presentation.HelpGenerator;
import es.upm.etsisi.park.presentation.SyntaxHelpFormatter;

import java.time.LocalDate;

/**
 * Composition root: injects the configured repository and collaborators into the singleton
 * controllers and builds the presentation layer. No dependency-injection framework is used.
 */
public final class ParkApplication {

    private ParkApplication() {
    }

    public static ParkServices start(ParkRepository repository, LocalDate today, PasswordHasher hasher) {
        Park park = Park.getInstance();
        park.start(repository, today);
        PassController passes = PassController.getInstance();
        passes.configure(park);
        UserController.getInstance().configure(park, hasher);
        GroupController.getInstance().configure(park);
        TicketController.getInstance().configure(park, passes);
        AttractionController.getInstance().configure(park, passes);
        InvoiceController.getInstance().configure(park);
        return new ParkServices(park, UserController.getInstance(), GroupController.getInstance(),
                TicketController.getInstance(), passes, AttractionController.getInstance(), InvoiceController.getInstance());
    }

    public static CommandProcessor commandProcessor(ParkServices services) {
        return new CommandProcessor(services, new HelpGenerator(new SyntaxHelpFormatter()),
                new HelpGenerator(new ExtendedHelpFormatter()));
    }
}
