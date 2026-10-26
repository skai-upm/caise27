package es.upm.parque.business.controller;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.persistence.AttractionRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for attractions and ticket-based access to them.
 */
public class AttractionController {

    private static final AttractionController INSTANCE = new AttractionController();

    private AttractionRepository repository;
    private TicketController ticketController;
    private SystemDate systemDate;
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborators of the controller and (re)loads its data. Called during application
     * initialization.
     */
    public AttractionController configure(AttractionRepository repository, TicketController ticketController,
                                          SystemDate systemDate) throws IOException {
        this.repository = repository;
        this.ticketController = ticketController;
        this.systemDate = systemDate;
        attractions.clear();
        for (Attraction attraction : repository.loadAll()) {
            attractions.put(attraction.getId(), attraction);
        }
        return this;
    }

    public Attraction find(String id) {
        requireConfigured();
        return attractions.get(id);
    }

    public List<Attraction> getAttractions() {
        requireConfigured();
        return new ArrayList<>(attractions.values());
    }

    public void create(String id, String name, String capacityText) {
        requireConfigured();
        if (id == null || id.isBlank()) {
            throw new BusinessException("The attraction identifier cannot be empty");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException("The attraction name cannot be empty");
        }
        if (attractions.containsKey(id)) {
            throw new BusinessException("An attraction with identifier " + id + " already exists");
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid capacity: " + capacityText);
        }
        if (capacity <= 0) {
            throw new BusinessException("Invalid capacity: " + capacityText + " (must be positive)");
        }
        attractions.put(id, new Attraction(id, name, capacity));
        save();
    }

    public void delete(String id) {
        requireConfigured();
        Attraction attraction = require(id);
        if (!attraction.getVisitors().isEmpty()) {
            throw new BusinessException("Attraction " + id + " has users inside and cannot be deleted");
        }
        attractions.remove(id);
        save();
    }

    public int enter(String attractionId, String ticketId) {
        requireConfigured();
        Attraction attraction = require(attractionId);
        return attraction.enter(requireTicket(ticketId), systemDate.get());
    }

    public int exit(String attractionId, String ticketId) {
        requireConfigured();
        Attraction attraction = require(attractionId);
        return attraction.exit(requireTicket(ticketId));
    }

    public Attraction require(String id) {
        requireConfigured();
        Attraction attraction = attractions.get(id);
        if (attraction == null) {
            throw new BusinessException("No attraction with identifier " + id);
        }
        return attraction;
    }

    private Ticket requireTicket(String ticketId) {
        Ticket ticket = ticketController.find(ticketId);
        if (ticket == null) {
            throw new BusinessException("No ticket with identifier " + ticketId);
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

    private void requireConfigured() {
        if (repository == null) {
            throw new IllegalStateException("AttractionController is not configured");
        }
    }
}
