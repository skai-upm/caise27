package es.upm.parque;

import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for attractions: creation, deletion, ticket-based entry and exit, and status queries.
 */
public class AttractionManager {
    private final AttractionRepository repository;
    private final TicketManager ticketManager;
    private final SystemDate systemDate;
    private final List<Attraction> attractions;

    public AttractionManager(AttractionRepository repository, TicketManager ticketManager, SystemDate systemDate) {
        this.repository = repository;
        this.ticketManager = ticketManager;
        this.systemDate = systemDate;
        this.attractions = new ArrayList<>(repository.load());
    }

    public Attraction create(String id, String name, int capacity) {
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
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        repository.save(attractions);
        return attraction;
    }

    public Attraction delete(String id) {
        Attraction attraction = require(id);
        if (!attraction.getPresent().isEmpty()) {
            throw new IllegalArgumentException("attraction " + id + " still has users inside");
        }
        attractions.remove(attraction);
        repository.save(attractions);
        return attraction;
    }

    public Attraction enter(String attractionId, String ticketId) {
        Attraction attraction = require(attractionId);
        Ticket ticket = requireTicket(ticketId);
        if (!ticket.getDate().equals(systemDate.get())) {
            throw new IllegalArgumentException("ticket " + ticketId + " is for " + ticket.getDate()
                    + " and is not valid on " + systemDate.get());
        }
        attraction.enter(ticket);
        return attraction;
    }

    public Attraction exit(String attractionId, String ticketId) {
        Attraction attraction = require(attractionId);
        attraction.exit(requireTicket(ticketId));
        return attraction;
    }

    public String status(String id) {
        Attraction a = require(id);
        String nl = System.lineSeparator();
        StringBuilder users = new StringBuilder();
        for (User u : a.getPresent()) {
            users.append(users.length() == 0 ? "" : ", ").append(u);
        }
        return "Attraction " + a.getId() + " - " + a.getName() + nl
                + "  Capacity: " + a.getCapacity() + nl
                + "  Present: " + a.getPresent().size() + nl
                + "  Available: " + a.getAvailable() + nl
                + "  Users present: " + (users.length() == 0 ? "none" : users);
    }

    public Attraction findById(String id) {
        for (Attraction a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public List<Attraction> getAttractions() {
        return List.copyOf(attractions);
    }

    private Attraction require(String id) {
        Attraction attraction = findById(id);
        if (attraction == null) {
            throw new IllegalArgumentException("attraction " + id + " does not exist");
        }
        return attraction;
    }

    private Ticket requireTicket(String id) {
        Ticket ticket = ticketManager.findById(id);
        if (ticket == null) {
            throw new IllegalArgumentException("ticket " + id + " does not exist");
        }
        return ticket;
    }
}
