package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.Attraction;
import es.upm.etsisi.park.business.model.AttractionType;
import es.upm.etsisi.park.business.model.EntryResult;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.AccessTicket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.validation.UserValidator;
import es.upm.etsisi.park.persistence.AttractionRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton managing attractions, their typed entry rules and occupancy.
 */
public final class AttractionController {

    private static AttractionController instance;

    private AttractionRepository repository;
    private SystemController system;
    private TicketController tickets;
    private PassController passes;
    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();

    private AttractionController() {
    }

    public static synchronized AttractionController getInstance() {
        if (instance == null) {
            instance = new AttractionController();
        }
        return instance;
    }

    public void initialize(AttractionRepository attractionRepository, SystemController systemController,
                           TicketController ticketController, PassController passController) {
        this.repository = attractionRepository;
        this.system = systemController;
        this.tickets = ticketController;
        this.passes = passController;
        attractions.clear();
        for (Attraction<?> attraction : attractionRepository.loadAll()) {
            attractions.put(attraction.getId(), attraction);
        }
    }

    /**
     * @param typeText attraction type, or {@code null} for GENERAL
     */
    public Attraction<?> create(String id, String name, String capacityText, String typeText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ValidationException("Attraction identifier and name must not be empty");
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid capacity: " + capacityText);
        }
        if (capacity <= 0) {
            throw new ValidationException("Invalid capacity: " + capacityText + ". It must be a positive integer");
        }
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.fromName(typeText);
        if (attractions.containsKey(id)) {
            throw new DuplicateException("Attraction " + id + " already exists");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.put(id, attraction);
        persist();
        return attraction;
    }

    public EntryResult enter(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = require(attractionId);
        Ticket ticket = tickets.require(ticketId);
        EntryResult result = enter(attraction, ticket, speed);
        if (!result.consumed().isEmpty()) {
            passes.persist();
        }
        return result;
    }

    private <T extends AccessTicket> EntryResult enter(Attraction<T> attraction, Ticket ticket, boolean speed) {
        T accepted = attraction.asAccepted(ticket);
        return attraction.enter(accepted, system.getCurrentDate(), speed);
    }

    public void exit(String attractionId, String email) {
        Attraction<?> attraction = require(attractionId);
        User user = attraction.findOccupant(email)
                .orElseThrow(() -> new NotFoundException("User " + email + " is not inside " + attractionId));
        attraction.release(user);
    }

    public void releaseEverywhere(User user) {
        for (Attraction<?> attraction : attractions.values()) {
            attraction.release(user);
        }
    }

    public Attraction<?> require(String id) {
        Attraction<?> attraction = attractions.get(id);
        if (attraction == null) {
            throw new NotFoundException("Attraction " + id + " does not exist");
        }
        return attraction;
    }

    public List<Attraction<?>> getAttractions() {
        return new ArrayList<>(attractions.values());
    }

    private void persist() {
        repository.saveAll(attractions.values());
    }
}
