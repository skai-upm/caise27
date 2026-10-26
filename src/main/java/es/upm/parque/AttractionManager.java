package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for attractions and ticket-based access to them.
 */
public class AttractionManager {

    private final AttractionRepository repository;
    private final TicketManager ticketManager;
    private final SystemDate systemDate;
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();

    public AttractionManager(AttractionRepository repository, TicketManager ticketManager, SystemDate systemDate)
            throws IOException {
        this.repository = repository;
        this.ticketManager = ticketManager;
        this.systemDate = systemDate;
        for (Attraction attraction : repository.loadAll()) {
            attractions.put(attraction.getId(), attraction);
        }
    }

    public Attraction find(String id) {
        return attractions.get(id);
    }

    public List<Attraction> getAttractions() {
        return new ArrayList<>(attractions.values());
    }

    public void create(String id, String name, String capacityText) {
        if (id == null || id.isBlank()) {
            throw new UserException("The attraction identifier cannot be empty");
        }
        if (name == null || name.isBlank()) {
            throw new UserException("The attraction name cannot be empty");
        }
        if (attractions.containsKey(id)) {
            throw new UserException("An attraction with identifier " + id + " already exists");
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            throw new UserException("Invalid capacity: " + capacityText);
        }
        if (capacity <= 0) {
            throw new UserException("Invalid capacity: " + capacityText + " (must be positive)");
        }
        attractions.put(id, new Attraction(id, name, capacity));
        save();
    }

    public void delete(String id) {
        Attraction attraction = require(id);
        if (!attraction.getVisitors().isEmpty()) {
            throw new UserException("Attraction " + id + " has users inside and cannot be deleted");
        }
        attractions.remove(id);
        save();
    }

    public int enter(String attractionId, String ticketId) {
        Attraction attraction = require(attractionId);
        return attraction.enter(requireTicket(ticketId), systemDate.get());
    }

    public int exit(String attractionId, String ticketId) {
        Attraction attraction = require(attractionId);
        return attraction.exit(requireTicket(ticketId));
    }

    public Attraction require(String id) {
        Attraction attraction = attractions.get(id);
        if (attraction == null) {
            throw new UserException("No attraction with identifier " + id);
        }
        return attraction;
    }

    private Ticket requireTicket(String ticketId) {
        Ticket ticket = ticketManager.find(ticketId);
        if (ticket == null) {
            throw new UserException("No ticket with identifier " + ticketId);
        }
        return ticket;
    }

    private void save() {
        try {
            repository.saveAll(attractions.values());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save attractions", e);
        }
    }
}
