package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Transfers the park state between the controller and the repositories stored
 * in one directory. Normal and temporary modes differ only in that directory.
 */
public class ParkStorage {

    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;

    public ParkStorage(Path directory) {
        users = new UserRepository(directory.resolve("users.txt"));
        groups = new GroupRepository(directory.resolve("groups.txt"));
        tickets = new TicketRepository(directory.resolve("tickets.txt"));
        attractions = new AttractionRepository(directory.resolve("attractions.txt"));
    }

    /**
     * Loads the stored state into {@code controller}; problems are reported on {@code out}.
     */
    public void load(ParkController controller, PrintStream out) {
        for (User user : users.load()) {
            report(out, () -> controller.addUser(user));
        }
        for (Group group : groups.load(controller::findUser)) {
            report(out, () -> {
                Group created = controller.createGroup(group.getIdentifier(), group.getName());
                group.getMembers().forEach(member -> controller.restoreGroupMember(created, member));
            });
        }
        List<User> historicalUsers = new ArrayList<>();
        for (Ticket ticket : tickets.load(controller::findGroup,
                (dni, email) -> resolveUser(controller, historicalUsers, dni, email))) {
            report(out, () -> controller.restoreTicket(ticket));
        }
        for (Attraction attraction : attractions.load()) {
            report(out, () -> controller.restoreAttraction(attraction));
        }
        List<String> problems = new ArrayList<>();
        problems.addAll(users.getProblems());
        problems.addAll(groups.getProblems());
        problems.addAll(tickets.getProblems());
        problems.addAll(attractions.getProblems());
        problems.forEach(problem -> out.println("ERROR: " + problem));
    }

    public void save(ParkController controller) {
        users.save(controller.getUsers());
        groups.save(controller.getGroups());
        tickets.save(controller.getTickets());
        attractions.save(controller.getAttractions());
    }

    /** The active user with that DNI and e-mail, otherwise one shared historical user record. */
    private static User resolveUser(ParkController controller, List<User> historical, String dni, String email) {
        Optional<User> active = controller.findUser(email).filter(user -> user.getDni().equals(dni));
        if (active.isPresent()) {
            return active.get();
        }
        for (User user : historical) {
            if (user.getDni().equals(dni) && user.getEmail().equals(email)) {
                return user;
            }
        }
        User user = new User(dni, email);
        historical.add(user);
        return user;
    }

    private static void report(PrintStream out, Runnable action) {
        try {
            action.run();
        } catch (ParkException e) {
            out.println("ERROR: Stored data ignored: " + e.getMessage());
        }
    }
}
