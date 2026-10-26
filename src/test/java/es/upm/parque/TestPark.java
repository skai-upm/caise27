package es.upm.parque;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.MainController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;
import es.upm.parque.presentation.CommandProcessor;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Builds the full application object graph over the data files of a test directory.
 * Creating a new instance over the same directory simulates restarting the application.
 */
public final class TestPark {

    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);

    public final MainController controller;
    public final UserController users;
    public final GroupController groups;
    public final TicketController tickets;
    public final AttractionController attractions;
    public final CommandProcessor processor;

    public TestPark(Path dir) {
        controller = new MainController(CLOCK);
        users = new UserController(new UserRepository(dir.resolve("users.txt")));
        groups = new GroupController(new GroupRepository(dir.resolve("groups.txt")), users);
        tickets = new TicketController(new TicketRepository(dir.resolve("tickets.txt"),
                dir.resolve("grouptickets.txt")), users, groups, controller);
        attractions = new AttractionController(new AttractionRepository(dir.resolve("attractions.txt")), tickets, controller);
        processor = new CommandProcessor(users, groups, tickets, attractions, controller);
    }

    public App app() {
        return new App(processor);
    }
}
