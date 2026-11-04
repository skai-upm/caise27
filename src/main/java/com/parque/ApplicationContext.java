package com.parque;

import com.parque.business.controller.ParkController;
import com.parque.business.service.AttractionService;
import com.parque.business.service.GroupService;
import com.parque.business.service.InvoiceDirector;
import com.parque.business.service.TicketService;
import com.parque.business.service.UserService;
import com.parque.persistence.AttractionRepository;
import com.parque.persistence.GroupRepository;
import com.parque.persistence.GroupTicketRepository;
import com.parque.persistence.TicketRepository;
import com.parque.persistence.UserRepository;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Composition root. At startup it creates and configures the repositories for the chosen data directory
 * (permanent or {@code --fly}), creates the services injecting them, and initializes the unique
 * {@link ParkController} with its collaborators. Repositories are ordinary objects, not Singletons.
 */
public final class ApplicationContext {
    public static final String USERS_FILE = "users.txt";
    public static final String GROUPS_FILE = "groups.txt";
    public static final String TICKETS_FILE = "tickets.txt";
    public static final String GROUP_TICKETS_FILE = "grouptickets.txt";
    public static final String ATTRACTIONS_FILE = "attractions.txt";

    private ApplicationContext() {
    }

    /** Wires every component over the data files of {@code dataDir} and initializes the controller. */
    public static ParkController start(Path dataDir) throws IOException {
        UserRepository userRepository = new UserRepository(dataDir.resolve(USERS_FILE));
        GroupRepository groupRepository = new GroupRepository(dataDir.resolve(GROUPS_FILE));
        TicketRepository ticketRepository = new TicketRepository(dataDir.resolve(TICKETS_FILE));
        GroupTicketRepository groupTicketRepository = new GroupTicketRepository(dataDir.resolve(GROUP_TICKETS_FILE));
        AttractionRepository attractionRepository = new AttractionRepository(dataDir.resolve(ATTRACTIONS_FILE));

        UserService users = new UserService(userRepository);
        GroupService groups = new GroupService(groupRepository, users);
        TicketService tickets = new TicketService(ticketRepository, groupTicketRepository, users, groups);
        AttractionService attractions = new AttractionService(attractionRepository);

        return ParkController.initialize(users, groups, tickets, attractions, new InvoiceDirector());
    }

    /** Ends the execution: releases the unique controller. */
    public static void stop() {
        ParkController.release();
    }
}
