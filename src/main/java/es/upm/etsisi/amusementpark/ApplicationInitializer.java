package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.controller.GroupController;
import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.controller.TicketController;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.business.security.PasswordHasher;
import es.upm.etsisi.amusementpark.persistence.Repositories;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.command.AttractionCommands;
import es.upm.etsisi.amusementpark.presentation.command.CommandModule;
import es.upm.etsisi.amusementpark.presentation.command.CommandRegistry;
import es.upm.etsisi.amusementpark.presentation.command.GroupCommands;
import es.upm.etsisi.amusementpark.presentation.command.InvoiceCommands;
import es.upm.etsisi.amusementpark.presentation.command.PassCommands;
import es.upm.etsisi.amusementpark.presentation.command.SystemCommands;
import es.upm.etsisi.amusementpark.presentation.command.TicketCommands;
import es.upm.etsisi.amusementpark.presentation.command.UserCommands;
import es.upm.etsisi.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;
import es.upm.etsisi.amusementpark.presentation.help.HelpMode;
import es.upm.etsisi.amusementpark.presentation.help.SyntaxHelpFormatter;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Composition root: creates the repositories for a data directory and injects them, and the
 * collaborations between controllers, into the singleton controllers and the presentation layer.
 */
public final class ApplicationInitializer {

    private ApplicationInitializer() {
    }

    public static CommandProcessor initialize(Path dataDir, PrintStream out) {
        return initialize(dataDir, new BCryptPasswordHasher(), out);
    }

    public static CommandProcessor initialize(Path dataDir, PasswordHasher hasher, PrintStream out) {
        ParkController park = ParkController.getInstance();
        park.configure(Repositories.fileBased(dataDir));

        PassController passes = PassController.getInstance();
        passes.configure(park);
        UserController users = UserController.getInstance();
        users.configure(park, hasher);
        GroupController groups = GroupController.getInstance();
        groups.configure(park);
        TicketController tickets = TicketController.getInstance();
        tickets.configure(park, passes);
        AttractionController attractions = AttractionController.getInstance();
        attractions.configure(park, passes);
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(park, passes);

        CommandRegistry registry = new CommandRegistry();
        List<CommandModule> modules = List.of(
                new UserCommands(users),
                new SystemCommands(park),
                new GroupCommands(groups),
                new TicketCommands(tickets),
                new AttractionCommands(attractions),
                new PassCommands(passes),
                new InvoiceCommands(invoices));
        modules.forEach(m -> m.registerIn(registry));

        HelpGenerator help = new HelpGenerator(Map.of(
                HelpMode.BASIC, new SyntaxHelpFormatter(),
                HelpMode.EXTENDED, new ExtendedHelpFormatter()));
        return new CommandProcessor(registry, help, out);
    }
}
