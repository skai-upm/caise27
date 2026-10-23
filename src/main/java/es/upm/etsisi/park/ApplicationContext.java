package es.upm.etsisi.park;

import java.io.PrintStream;
import java.util.Map;

import es.upm.etsisi.park.business.controller.AttractionController;
import es.upm.etsisi.park.business.controller.GroupController;
import es.upm.etsisi.park.business.controller.InvoiceController;
import es.upm.etsisi.park.business.controller.PassController;
import es.upm.etsisi.park.business.controller.SystemController;
import es.upm.etsisi.park.business.controller.TicketController;
import es.upm.etsisi.park.business.controller.UserController;
import es.upm.etsisi.park.business.exception.PersistenceException;
import es.upm.etsisi.park.business.security.PasswordHasher;
import es.upm.etsisi.park.persistence.Repositories;
import es.upm.etsisi.park.presentation.CommandProcessor;
import es.upm.etsisi.park.presentation.CommandRegistry;
import es.upm.etsisi.park.presentation.command.AttractionCommands;
import es.upm.etsisi.park.presentation.command.GeneralCommands;
import es.upm.etsisi.park.presentation.command.GroupCommands;
import es.upm.etsisi.park.presentation.command.InvoiceCommands;
import es.upm.etsisi.park.presentation.command.PassCommands;
import es.upm.etsisi.park.presentation.command.SystemCommands;
import es.upm.etsisi.park.presentation.command.TicketCommands;
import es.upm.etsisi.park.presentation.command.UserCommands;
import es.upm.etsisi.park.presentation.help.ExtendedHelpRenderer;
import es.upm.etsisi.park.presentation.help.HelpGenerator;
import es.upm.etsisi.park.presentation.help.SyntaxHelpRenderer;

/**
 * Composition root: injects the repositories and collaborators into the
 * singleton controllers, loads the stored state, and builds the
 * presentation layer on top of them. Dependency injection is done by hand.
 */
public final class ApplicationContext {

    private ApplicationContext() {
    }

    /**
     * Configures every controller with the given repositories and loads the
     * data. Calling it again replaces the whole park state.
     */
    public static void initialize(Repositories repositories, PasswordHasher hasher) throws PersistenceException {
        SystemController system = SystemController.getInstance();
        UserController users = UserController.getInstance();
        GroupController groups = GroupController.getInstance();
        TicketController tickets = TicketController.getInstance();
        PassController passes = PassController.getInstance();
        AttractionController attractions = AttractionController.getInstance();
        InvoiceController invoices = InvoiceController.getInstance();

        system.reset();
        users.configure(repositories.users(), hasher, tickets, groups);
        groups.configure(repositories.groups(), system, users, tickets);
        tickets.configure(repositories.tickets(), system, users, groups, passes);
        passes.configure(repositories.passes(), system, tickets);
        attractions.configure(repositories.attractions(), system, tickets, passes);
        invoices.configure(system, users, groups, tickets);

        users.load();
        groups.load();
        tickets.load();
        passes.load();
        attractions.load();
    }

    /** The command processor wired to the configured controllers. */
    public static CommandProcessor commandProcessor(PrintStream out) {
        CommandRegistry registry = new CommandRegistry();
        HelpGenerator help = new HelpGenerator(registry, Map.of(
                HelpGenerator.BASIC, new SyntaxHelpRenderer(),
                HelpGenerator.EXTENDED, new ExtendedHelpRenderer()));
        registry.registerAll(
                new UserCommands(UserController.getInstance()),
                new SystemCommands(SystemController.getInstance()),
                new GroupCommands(GroupController.getInstance()),
                new TicketCommands(TicketController.getInstance()),
                new AttractionCommands(AttractionController.getInstance()),
                new PassCommands(PassController.getInstance()),
                new InvoiceCommands(InvoiceController.getInstance()),
                new GeneralCommands(help));
        return new CommandProcessor(registry, out);
    }
}
