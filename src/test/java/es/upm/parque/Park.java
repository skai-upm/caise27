package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Test fixture: the whole application wired over files in one directory.
 */
final class Park {

    final SystemDate date;
    final UserManager users;
    final GroupManager groups;
    final TicketManager tickets;
    final AttractionManager attractions;
    final CommandProcessor processor;

    Park(Path dir, LocalDate today) {
        try {
            date = new SystemDate(today);
            users = new UserManager(new UserRepository(dir.resolve("users.txt")));
            groups = new GroupManager(new GroupRepository(dir.resolve("groups.txt")), users);
            tickets = new TicketManager(new TicketRepository(dir.resolve("tickets.txt")),
                    new GroupTicketRepository(dir.resolve("grouptickets.txt")), users, groups, date);
            attractions = new AttractionManager(new AttractionRepository(dir.resolve("attractions.txt")), tickets,
                    date);
            processor = new CommandProcessor(users, groups, tickets, attractions, date);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String run(String command) {
        return processor.process(command);
    }
}
