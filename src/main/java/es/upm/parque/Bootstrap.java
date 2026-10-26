package es.upm.parque;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import es.upm.parque.business.controller.AttractionManager;
import es.upm.parque.business.controller.Controllers;
import es.upm.parque.business.controller.GroupManager;
import es.upm.parque.business.controller.InvoiceManager;
import es.upm.parque.business.controller.PassManager;
import es.upm.parque.business.controller.TicketManager;
import es.upm.parque.business.controller.UserManager;
import es.upm.parque.business.model.StandardInvoiceBuilder;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.security.BCryptPasswordHasher;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.persistence.PassRepository;
import es.upm.parque.persistence.TextFileAttractionRepository;
import es.upm.parque.persistence.TextFileGroupRepository;
import es.upm.parque.persistence.TextFileGroupTicketRepository;
import es.upm.parque.persistence.TextFilePassRepository;
import es.upm.parque.persistence.TextFileTicketRepository;
import es.upm.parque.persistence.TextFileUserRepository;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.CommandRegistry;
import es.upm.parque.presentation.ExtendedHelpFormat;
import es.upm.parque.presentation.HelpGenerator;
import es.upm.parque.presentation.SyntaxHelpFormat;

/**
 * Startup configuration: creates the repositories for a data directory and initialises the
 * singleton controllers, injecting into each one the repositories, date and controllers it needs.
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    static final int BCRYPT_LOG_ROUNDS = 10;

    public static CommandProcessor configure(Path dataDir, LocalDate today) throws IOException {
        return configure(dataDir, today, new BCryptPasswordHasher(BCRYPT_LOG_ROUNDS));
    }

    public static CommandProcessor configure(Path dataDir, LocalDate today, PasswordHasher hasher)
            throws IOException {
        SystemDate systemDate = new SystemDate(today);
        UserManager users = UserManager.initialize(
                new TextFileUserRepository(dataDir.resolve("users.txt")), hasher);
        GroupManager groups = GroupManager.initialize(
                new TextFileGroupRepository(dataDir.resolve("groups.txt")), users);
        PassRepository passRepository = new TextFilePassRepository(dataDir.resolve("passes.txt"));
        TicketManager tickets = TicketManager.initialize(
                new TextFileTicketRepository(dataDir.resolve("tickets.txt")),
                new TextFileGroupTicketRepository(dataDir.resolve("grouptickets.txt")), passRepository,
                users, groups, systemDate);
        PassManager passes = PassManager.initialize(passRepository, tickets, systemDate);
        AttractionManager attractions = AttractionManager.initialize(
                new TextFileAttractionRepository(dataDir.resolve("attractions.txt")), tickets, passes, systemDate);
        InvoiceManager invoices = InvoiceManager.initialize(users, groups, tickets, systemDate,
                StandardInvoiceBuilder::new);
        Controllers controllers = new Controllers(users, groups, tickets, attractions, invoices, passes);
        return new CommandProcessor(controllers, systemDate, helpModes(CommandRegistry.standard()));
    }

    /**
     * Help commands and the presentation format injected into each help generator.
     */
    static Map<String, HelpGenerator> helpModes(CommandRegistry registry) {
        Map<String, HelpGenerator> modes = new LinkedHashMap<>();
        modes.put("HELP", new HelpGenerator(registry, new SyntaxHelpFormat()));
        modes.put("HELP EXTENDED", new HelpGenerator(registry, new ExtendedHelpFormat()));
        return modes;
    }
}
