package es.upm.etsisi.park.service;

import es.upm.etsisi.park.core.SystemState;
import es.upm.etsisi.park.model.Attraction;
import es.upm.etsisi.park.model.Ticket;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.repository.AttractionRepository;
import es.upm.etsisi.park.repository.TicketRepository;
import es.upm.etsisi.park.repository.UserRepository;
import es.upm.etsisi.park.validation.UserValidator;

import java.util.List;

/**
 * ATTRACTION CREATE / ENTER / EXIT / LIST / STATUS. Entry works the same for every ticket
 * type through {@link Ticket#getUsers()}.
 */
public class AttractionService {

    private final AttractionRepository attractions;
    private final TicketRepository tickets;
    private final UserRepository users;
    private final SystemState state;

    public AttractionService(AttractionRepository attractions, TicketRepository tickets, UserRepository users,
                             SystemState state) {
        this.attractions = attractions;
        this.tickets = tickets;
        this.users = users;
        this.state = state;
    }

    public Attraction create(String id, String name, String capacityText) {
        Checks.require(UserValidator.isNotBlank(id), "Attraction identifier must not be empty");
        Checks.require(UserValidator.isNotBlank(name), "Attraction name must not be empty");
        Checks.require(attractions.findById(id).isEmpty(), "An attraction with identifier " + id + " already exists");
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            throw new UserOperationException("Invalid capacity: " + capacityText);
        }
        Checks.require(capacity > 0, "Capacity must be a positive number");
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        return attraction;
    }

    /** Admits every user of the ticket, or nobody. */
    public List<User> enter(String attractionId, String ticketId) {
        Attraction attraction = find(attractionId);
        Ticket ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new UserOperationException("Ticket not found: " + ticketId));
        Checks.require(ticket.isValidOn(state.getCurrentDate()),
                "Ticket " + ticketId + " is valid on " + ticket.getDate() + ", not on " + state.getCurrentDate());
        List<User> entering = ticket.getUsers();
        for (User user : entering) {
            Checks.require(users.findAll().contains(user), "User " + user.getEmail() + " is no longer registered");
            attractions.findContaining(user).ifPresent(current -> {
                throw new UserOperationException("User " + user.getEmail() + " is already inside " + current.getId());
            });
        }
        Checks.require(attraction.enter(entering), "Not enough capacity in " + attractionId + ": "
                + attraction.getAvailablePlaces() + " places available, " + entering.size() + " needed");
        return entering;
    }

    public User exit(String attractionId, String email) {
        Attraction attraction = find(attractionId);
        User user = users.findByEmail(email)
                .filter(attraction::isInside)
                .orElseThrow(() -> new UserOperationException("User " + email + " is not inside " + attractionId));
        attraction.exit(user);
        return user;
    }

    public Attraction find(String attractionId) {
        return attractions.findById(attractionId)
                .orElseThrow(() -> new UserOperationException("Attraction not found: " + attractionId));
    }
}
