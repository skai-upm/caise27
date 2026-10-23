package es.upm.etsisi.park.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.PersistenceException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.attraction.AttractionType;
import es.upm.etsisi.park.business.model.attraction.Entry;
import es.upm.etsisi.park.business.model.ticket.Ticket;
import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.persistence.AttractionRepository;

/**
 * Manages attractions, entries and exits. The type-specific access and pass
 * rules live in the attraction classes; this controller resolves the
 * participants and persists consumed passes. Singleton.
 */
public final class AttractionController {

    public static final String SPEED_OPTION = "SPEED";

    private static final AttractionController INSTANCE = new AttractionController();

    private AttractionRepository repository;
    private SystemController system;
    private TicketController tickets;
    private PassController passes;
    private final List<Attraction<?>> attractions = new ArrayList<>();

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    public void configure(AttractionRepository repository, SystemController system, TicketController tickets,
                          PassController passes) {
        this.repository = repository;
        this.system = system;
        this.tickets = tickets;
        this.passes = passes;
        attractions.clear();
    }

    public void load() throws PersistenceException {
        attractions.clear();
        try {
            attractions.addAll(repository.load());
        } catch (IOException e) {
            throw new PersistenceException("Could not load attractions: " + e.getMessage(), e);
        }
    }

    public List<Attraction<?>> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Attraction<?> find(String id) {
        for (Attraction<?> attraction : attractions) {
            if (attraction.getId().equalsIgnoreCase(id)) {
                return attraction;
            }
        }
        return null;
    }

    public Attraction<?> require(String id) throws NotFoundException {
        Attraction<?> attraction = find(id);
        if (attraction == null) {
            throw new NotFoundException("Attraction " + id + " not found");
        }
        return attraction;
    }

    /** Creates an attraction; a null type means GENERAL (Delivery 2 syntax). */
    public Attraction<?> create(String id, String name, String capacityText, String typeText) throws ParkException {
        Checks.requireText(id, "Attraction identifier");
        Checks.requireText(name, "Attraction name");
        int capacity = parseCapacity(capacityText);
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.parse(typeText);
        if (find(id) != null) {
            throw new DuplicateException("Attraction " + id + " already exists");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.add(attraction);
        persist();
        return attraction;
    }

    /**
     * Lets the holders of a ticket in, optionally with SpeedPasses
     * ({@code option} = SPEED, or null). Nothing changes if any rule fails.
     */
    public Entry enter(String attractionId, String ticketId, String option) throws ParkException {
        Attraction<?> attraction = require(attractionId);
        Ticket ticket = tickets.require(ticketId);
        boolean speed = parseOption(option);
        for (User user : ticket.getHolders()) {
            Attraction<?> current = attractionOf(user);
            if (current != null) {
                throw new OperationNotAllowedException("User " + user.getEmail() + " is already inside attraction "
                        + current.getId());
            }
        }
        Entry entry = attraction.enter(ticket, speed, system.getDate());
        if (!entry.consumedPasses().isEmpty()) {
            passes.persist();
        }
        return entry;
    }

    public User exit(String attractionId, String email) throws ParkException {
        Attraction<?> attraction = require(attractionId);
        User user = attraction.findOccupantByEmail(email);
        if (user == null) {
            throw new NotFoundException("User " + email + " is not inside attraction " + attraction.getId());
        }
        attraction.exit(user);
        return user;
    }

    private Attraction<?> attractionOf(User user) {
        for (Attraction<?> attraction : attractions) {
            if (attraction.contains(user)) {
                return attraction;
            }
        }
        return null;
    }

    private static boolean parseOption(String option) throws ValidationException {
        if (option == null) {
            return false;
        }
        if (SPEED_OPTION.equalsIgnoreCase(option.trim())) {
            return true;
        }
        throw new ValidationException("Invalid entry option '" + option + "': only " + SPEED_OPTION + " is allowed");
    }

    private static int parseCapacity(String text) throws ValidationException {
        try {
            int capacity = Integer.parseInt(text);
            if (capacity > 0) {
                return capacity;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        throw new ValidationException("Invalid capacity '" + text + "': expected a positive integer");
    }

    private void persist() throws PersistenceException {
        try {
            repository.save(attractions);
        } catch (IOException e) {
            throw new PersistenceException("Could not save attractions: " + e.getMessage(), e);
        }
    }
}
