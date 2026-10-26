package es.upm.parque;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Builds the full application object graph over the data files of a test directory.
 * Creating a new instance over the same directory simulates restarting the application.
 */
final class TestPark {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);

    final MainController controller;
    final UserManager users;
    final GroupManager groups;
    final TicketManager tickets;
    final AttractionManager attractions;
    final CommandProcessor processor;

    TestPark(Path dir) {
        controller = new MainController(CLOCK);
        users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        groups = new GroupManager(new GroupRepository(dir.resolve("groups.txt")), users);
        tickets = new TicketManager(new TicketRepository(dir.resolve("tickets.txt"),
                dir.resolve("grouptickets.txt")), users, groups, controller);
        attractions = new AttractionManager(new AttractionRepository(dir.resolve("attractions.txt")), tickets, controller);
        processor = new CommandProcessor(users, groups, tickets, attractions, controller);
    }

    App app() {
        return new App(processor);
    }
}
