package es.upm.parque;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.StandardInvoiceBuilder;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.persistence.FileAttractionRepository;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FilePassRepository;
import es.upm.parque.persistence.FileTicketRepository;
import es.upm.parque.persistence.FileUserRepository;
import es.upm.parque.presentation.AttractionCommands;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.CommandRegistry;
import es.upm.parque.presentation.CommandSet;
import es.upm.parque.presentation.GroupCommands;
import es.upm.parque.presentation.HelpCommands;
import es.upm.parque.presentation.InvoiceCommands;
import es.upm.parque.presentation.PassCommands;
import es.upm.parque.presentation.SystemCommands;
import es.upm.parque.presentation.TicketCommands;
import es.upm.parque.presentation.UserCommands;
import es.upm.parque.presentation.help.ExtendedHelpFormat;
import es.upm.parque.presentation.help.HelpGenerator;
import es.upm.parque.presentation.help.SyntaxHelpFormat;

/**
 * Composition root: creates the repositories for a data directory, configures the singleton
 * controllers with them and with each other, loads the stored data, and builds the command processor.
 */
public final class ParkInitializer {

    private ParkInitializer() {
    }

    public static CommandProcessor initialize(Path dataDir, LocalDate today, PasswordHasher hasher, PrintStream out) {
        ParkController park = ParkController.getInstance();
        UserController users = UserController.getInstance();
        GroupController groups = GroupController.getInstance();
        TicketController tickets = TicketController.getInstance();
        PassController passes = PassController.getInstance();
        AttractionController attractions = AttractionController.getInstance();
        InvoiceController invoices = InvoiceController.getInstance();

        park.configure(today);
        users.configure(new FileUserRepository(dataDir.resolve("users.txt")), hasher, park, groups, tickets);
        groups.configure(new FileGroupRepository(dataDir.resolve("groups.txt")), park, users, tickets);
        tickets.configure(new FileTicketRepository(dataDir.resolve("tickets.txt"),
                dataDir.resolve("grouptickets.txt")), park, users, groups, passes);
        passes.configure(new FilePassRepository(dataDir.resolve("passes.txt")), park, tickets);
        attractions.configure(new FileAttractionRepository(dataDir.resolve("attractions.txt")), park, tickets,
                passes);
        invoices.configure(StandardInvoiceBuilder::new, users, groups, tickets);

        users.load();
        groups.load();
        tickets.load();
        passes.load();
        attractions.load();

        CommandRegistry registry = new CommandRegistry();
        List<CommandSet> commandSets = List.of(
                new UserCommands(users),
                new SystemCommands(park),
                new GroupCommands(groups),
                new TicketCommands(tickets),
                new PassCommands(passes),
                new AttractionCommands(attractions),
                new InvoiceCommands(invoices),
                new HelpCommands(new HelpGenerator(new SyntaxHelpFormat()),
                        new HelpGenerator(new ExtendedHelpFormat())));
        commandSets.forEach(set -> set.registerInto(registry));
        return new CommandProcessor(registry, out);
    }
}
