package com.amusementpark.presentation;

import com.amusementpark.business.controller.AttractionController;
import com.amusementpark.business.controller.GroupController;
import com.amusementpark.business.controller.InvoiceController;
import com.amusementpark.business.controller.ParkController;
import com.amusementpark.business.controller.TicketController;
import com.amusementpark.business.controller.UserController;
import com.amusementpark.business.invoice.StandardInvoiceBuilder;
import com.amusementpark.business.security.PasswordHasher;
import com.amusementpark.persistence.AttractionRepository;
import com.amusementpark.persistence.FileAttractionRepository;
import com.amusementpark.persistence.FileGroupRepository;
import com.amusementpark.persistence.FileGroupTicketRepository;
import com.amusementpark.persistence.FilePassRepository;
import com.amusementpark.persistence.FileTicketRepository;
import com.amusementpark.persistence.FileUserRepository;
import com.amusementpark.persistence.GroupRepository;
import com.amusementpark.persistence.GroupTicketRepository;
import com.amusementpark.persistence.PassRepository;
import com.amusementpark.persistence.TicketRepository;
import com.amusementpark.persistence.UserRepository;
import com.amusementpark.presentation.help.CommandCatalog;
import com.amusementpark.presentation.help.ExtendedHelpFormat;
import com.amusementpark.presentation.help.HelpGenerator;
import com.amusementpark.presentation.help.SyntaxHelpFormat;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

/**
 * Application initialisation: creates the repositories for a data directory, injects them and
 * the collaborating controllers into the singleton controllers, and loads the stored data.
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    /**
     * Configures the application to use the files in {@code dataDir} and returns the command
     * processor writing to {@code out}.
     */
    public static CommandProcessor initialize(Path dataDir, LocalDate today, PrintStream out) throws IOException {
        return initialize(dataDir, today, new PasswordHasher(PasswordHasher.DEFAULT_LOG_ROUNDS), out);
    }

    /**
     * As {@link #initialize(Path, LocalDate, PrintStream)} with an explicit password hasher.
     */
    public static CommandProcessor initialize(Path dataDir, LocalDate today, PasswordHasher hasher,
                                              PrintStream out) throws IOException {
        UserRepository userRepository = new FileUserRepository(dataDir.resolve("users.txt"));
        GroupRepository groupRepository = new FileGroupRepository(dataDir.resolve("groups.txt"));
        TicketRepository ticketRepository = new FileTicketRepository(dataDir.resolve("tickets.txt"));
        GroupTicketRepository groupTicketRepository =
                new FileGroupTicketRepository(dataDir.resolve("group_tickets.txt"));
        PassRepository passRepository = new FilePassRepository(dataDir.resolve("passes.txt"));
        AttractionRepository attractionRepository = new FileAttractionRepository(dataDir.resolve("attractions.txt"));

        ParkController park = ParkController.getInstance();
        UserController users = UserController.getInstance();
        GroupController groups = GroupController.getInstance();
        TicketController tickets = TicketController.getInstance();
        AttractionController attractions = AttractionController.getInstance();
        InvoiceController invoices = InvoiceController.getInstance();

        park.configure(today);
        users.configure(userRepository, hasher, tickets, groups);
        groups.configure(groupRepository, users, tickets);
        tickets.configure(ticketRepository, groupTicketRepository, passRepository, park, users, groups);
        attractions.configure(attractionRepository, park, tickets);
        invoices.configure(StandardInvoiceBuilder::new, users, groups, tickets);

        users.load();
        groups.load();
        tickets.load();
        attractions.load();
        HelpGenerator help = new HelpGenerator(CommandCatalog.commands(),
                Map.of("", new SyntaxHelpFormat(), "EXTENDED", new ExtendedHelpFormat()));
        return new CommandProcessor(park, users, groups, tickets, attractions, invoices, help, out);
    }
}
