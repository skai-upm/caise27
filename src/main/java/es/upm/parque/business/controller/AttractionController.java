package es.upm.parque.business.controller;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.AttractionRepository;
import es.upm.parque.business.validation.UserValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for attractions: creation, deletion, ticket-based entry and exit, and status queries.
 */
public class AttractionController {
    private static final AttractionController INSTANCE = new AttractionController();

    private AttractionRepository repository;
    private TicketController ticketController;
    private PassController passController;
    private SystemDate systemDate;
    private final List<Attraction<?>> attractions = new ArrayList<>();

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repository and collaborators and reloads the attractions (always empty of users).
     */
    public AttractionController configure(AttractionRepository repository, TicketController ticketController,
            PassController passController, SystemDate systemDate) {
        this.repository = repository;
        this.ticketController = ticketController;
        this.passController = passController;
        this.systemDate = systemDate;
        attractions.clear();
        attractions.addAll(repository.load());
        return this;
    }

    public Attraction<?> create(String id, String name, int capacity, AttractionType type) {
        if (!UserValidator.isNotBlank(id)) {
            throw new IllegalArgumentException("the attraction identifier cannot be empty");
        }
        if (!UserValidator.isNotBlank(name)) {
            throw new IllegalArgumentException("the attraction name cannot be empty");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("the capacity must be a positive integer");
        }
        if (findById(id) != null) {
            throw new IllegalArgumentException("attraction " + id + " already exists");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.add(attraction);
        repository.save(attractions);
        return attraction;
    }

    public Attraction<?> delete(String id) {
        Attraction<?> attraction = require(id);
        if (!attraction.getPresent().isEmpty()) {
            throw new IllegalArgumentException("attraction " + id + " still has users inside");
        }
        attractions.remove(attraction);
        repository.save(attractions);
        return attraction;
    }

    public List<Pass> enter(String attractionId, String ticketId) {
        return enter(attractionId, ticketId, false);
    }

    /**
     * Lets the ticket's users into the attraction, using SpeedPasses when {@code speed} is requested and the
     * Special Pass at special attractions. Consumed passes are persisted.
     *
     * @return the passes consumed by the entry
     */
    public List<Pass> enter(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = require(attractionId);
        Ticket ticket = requireTicket(ticketId);
        if (!ticket.getDate().equals(systemDate.get())) {
            throw new IllegalArgumentException("ticket " + ticketId + " is for " + ticket.getDate()
                    + " and is not valid on " + systemDate.get());
        }
        List<Pass> consumed = attraction.admit(ticket, speed);
        if (!consumed.isEmpty()) {
            passController.save();
        }
        return consumed;
    }

    public Attraction<?> exit(String attractionId, String ticketId) {
        Attraction<?> attraction = require(attractionId);
        attraction.exit(requireTicket(ticketId));
        return attraction;
    }

    public String status(String id) {
        Attraction<?> a = require(id);
        String nl = System.lineSeparator();
        StringBuilder users = new StringBuilder();
        for (User u : a.getPresent()) {
            users.append(users.length() == 0 ? "" : ", ").append(u);
        }
        return "Attraction " + a.getId() + " - " + a.getName() + nl
                + "  Type: " + a.getType() + nl
                + "  Capacity: " + a.getCapacity() + nl
                + "  Present: " + a.getPresent().size() + nl
                + "  Available: " + a.getAvailable() + nl
                + "  Users present: " + (users.length() == 0 ? "none" : users);
    }

    public Attraction<?> findById(String id) {
        for (Attraction<?> a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public List<Attraction<?>> getAttractions() {
        return List.copyOf(attractions);
    }

    private Attraction<?> require(String id) {
        Attraction<?> attraction = findById(id);
        if (attraction == null) {
            throw new IllegalArgumentException("attraction " + id + " does not exist");
        }
        return attraction;
    }

    private Ticket requireTicket(String id) {
        Ticket ticket = ticketController.findById(id);
        if (ticket == null) {
            throw new IllegalArgumentException("ticket " + id + " does not exist");
        }
        return ticket;
    }
}
