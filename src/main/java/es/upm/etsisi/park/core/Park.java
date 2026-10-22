package es.upm.etsisi.park.core;

import es.upm.etsisi.park.persistence.AttractionFileStore;
import es.upm.etsisi.park.persistence.GroupFileStore;
import es.upm.etsisi.park.persistence.TicketFileStore;
import es.upm.etsisi.park.persistence.UserFileStore;
import es.upm.etsisi.park.repository.AttractionRepository;
import es.upm.etsisi.park.repository.GroupRepository;
import es.upm.etsisi.park.repository.TicketRepository;
import es.upm.etsisi.park.repository.UserRepository;
import es.upm.etsisi.park.service.AttractionService;
import es.upm.etsisi.park.service.GroupService;
import es.upm.etsisi.park.service.SystemService;
import es.upm.etsisi.park.service.TicketService;
import es.upm.etsisi.park.service.UserService;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Central park management. Exactly one instance exists per execution; it owns the system
 * state and gives access to users, groups, tickets and attractions.
 */
public final class Park {

    private static final Park INSTANCE = new Park();

    private SystemState state;
    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private AttractionRepository attractions;

    private SystemService systemService;
    private UserService userService;
    private GroupService groupService;
    private TicketService ticketService;
    private AttractionService attractionService;

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    /**
     * Loads (or reloads) the park from the data files of {@code dataDirectory}. The system
     * date is reset to today and the base price to 40.00.
     */
    public void load(Path dataDirectory) {
        DataPaths paths = new DataPaths(dataDirectory);
        state = new SystemState(LocalDate.now(ZoneId.systemDefault()));
        users = new UserRepository(new UserFileStore(paths.users()));
        groups = new GroupRepository(new GroupFileStore(paths.groups()), users);
        attractions = new AttractionRepository(new AttractionFileStore(paths.attractions()));
        tickets = new TicketRepository(new TicketFileStore(paths.tickets()), users, groups);

        systemService = new SystemService(state);
        userService = new UserService(users, groups, tickets, attractions, state);
        groupService = new GroupService(groups, users, tickets, state);
        ticketService = new TicketService(tickets, users, groups, state);
        attractionService = new AttractionService(attractions, tickets, users, state);
    }

    public SystemState getState() {
        return requireLoaded(state);
    }

    public UserRepository getUsers() {
        return requireLoaded(users);
    }

    public GroupRepository getGroups() {
        return requireLoaded(groups);
    }

    public TicketRepository getTickets() {
        return requireLoaded(tickets);
    }

    public AttractionRepository getAttractions() {
        return requireLoaded(attractions);
    }

    public SystemService getSystemService() {
        return requireLoaded(systemService);
    }

    public UserService getUserService() {
        return requireLoaded(userService);
    }

    public GroupService getGroupService() {
        return requireLoaded(groupService);
    }

    public TicketService getTicketService() {
        return requireLoaded(ticketService);
    }

    public AttractionService getAttractionService() {
        return requireLoaded(attractionService);
    }

    private static <T> T requireLoaded(T component) {
        if (component == null) {
            throw new IllegalStateException("Park has not been loaded");
        }
        return component;
    }
}
