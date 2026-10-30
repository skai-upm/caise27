package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.controller.SystemController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.file.FileRepositories;
import es.upm.amusementpark.presentation.CommandCatalog;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.ExtendedHelpFormatter;
import es.upm.amusementpark.presentation.HelpGenerator;
import es.upm.amusementpark.presentation.SyntaxHelpFormatter;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Composition root: creates the repositories for a data directory and injects them, together with the
 * collaborations between controllers, into the Singleton controllers and the presentation layer.
 */
public final class ApplicationInitializer {

    private ApplicationInitializer() {
    }

    public static CommandProcessor initialize(Path dataDirectory, PasswordHasher hasher) {
        FileRepositories repositories = FileRepositories.open(dataDirectory);

        Park park = Park.getInstance();
        park.configure(repositories.users(), repositories.groups(), repositories.tickets(),
                repositories.attractions(), repositories.passes());

        SystemController system = SystemController.getInstance();
        system.configure(park);
        PassController passes = PassController.getInstance();
        passes.configure(park, repositories.passes(), repositories.tickets());
        UserController users = UserController.getInstance();
        users.configure(park, repositories.users(), repositories.groups(), repositories.tickets(), hasher);
        GroupController groups = GroupController.getInstance();
        groups.configure(park, repositories.groups(), repositories.users(), repositories.tickets());
        TicketController tickets = TicketController.getInstance();
        tickets.configure(park, repositories.tickets(), repositories.users(), repositories.groups(), passes);
        AttractionController attractions = AttractionController.getInstance();
        attractions.configure(park, repositories.attractions(), repositories.tickets(), repositories.users(), passes);
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(park, repositories.users(), repositories.groups(), repositories.tickets(),
                repositories.passes());

        Map<String, HelpGenerator> help = new LinkedHashMap<>();
        help.put("HELP", new HelpGenerator(CommandCatalog.commands(), new SyntaxHelpFormatter()));
        help.put("HELP EXTENDED", new HelpGenerator(CommandCatalog.commands(), new ExtendedHelpFormatter()));

        return new CommandProcessor(users, system, groups, tickets, passes, attractions, invoices, help);
    }
}
