package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.attraction.Attraction;
import es.upm.amusementpark.business.model.attraction.AttractionType;
import es.upm.amusementpark.business.model.attraction.EntryResult;
import es.upm.amusementpark.business.model.ticket.Ticket;
import es.upm.amusementpark.business.repository.AttractionRepository;
import es.upm.amusementpark.business.validation.ValueParser;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages attractions and entry to / exit from them.
 */
public final class AttractionController {

    private static final AttractionController INSTANCE = new AttractionController();

    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();
    private AttractionRepository repository;
    private SystemController system;
    private UserController users;
    private TicketController tickets;
    private PassController passes;

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    public void configure(AttractionRepository repository) {
        this.repository = repository;
    }

    public void setCollaborators(SystemController system, UserController users, TicketController tickets,
                                 PassController passes) {
        this.system = system;
        this.users = users;
        this.tickets = tickets;
        this.passes = passes;
    }

    public void load() throws PersistenceException {
        attractions.clear();
        try {
            for (Attraction<?> attraction : repository.load()) {
                attractions.put(attraction.getId(), attraction);
            }
        } catch (IOException e) {
            throw new PersistenceException("could not load attractions: " + e.getMessage(), e);
        }
    }

    public void save() throws PersistenceException {
        try {
            repository.saveAll(attractions.values());
        } catch (IOException e) {
            throw new PersistenceException("could not save attractions: " + e.getMessage(), e);
        }
    }

    /**
     * Creates an attraction; {@code typeName} null means GENERAL (Delivery 2 syntax).
     */
    public Attraction<?> create(String id, String name, String capacity, String typeName) throws ParkException {
        if (id.isEmpty()) {
            throw new ValidationException("attraction identifier must not be empty");
        }
        int places = ValueParser.parsePositiveInt(capacity);
        AttractionType type = typeName == null ? AttractionType.GENERAL : AttractionType.fromName(typeName);
        if (type == null) {
            throw new ValidationException("unknown attraction type: " + typeName);
        }
        if (attractions.containsKey(id)) {
            throw new DuplicateException("attraction already exists: " + id);
        }
        Attraction<?> attraction = type.create(id, name, places);
        attractions.put(id, attraction);
        return attraction;
    }

    /**
     * Admits everyone covered by a ticket valid today, consuming passes as the attraction requires.
     */
    public EntryResult enter(String attractionId, String ticketId, boolean speed) throws ParkException {
        Attraction<?> attraction = requireAttraction(attractionId);
        Ticket ticket = tickets.requireTicket(ticketId);
        for (User user : ticket.getUsers()) {
            if (!users.isActive(user)) {
                throw new OperationNotAllowedException("user is no longer active: " + user.getEmail());
            }
        }
        return attraction.admit(ticket, speed, passes, system.getCurrentDate());
    }

    public void exit(String attractionId, String email) throws ParkException {
        Attraction<?> attraction = requireAttraction(attractionId);
        for (User occupant : attraction.getOccupants()) {
            if (occupant.getEmail().equals(email)) {
                attraction.exit(occupant);
                return;
            }
        }
        throw new NotFoundException("user is not inside the attraction: " + email);
    }

    public void removeEverywhere(User user) {
        for (Attraction<?> attraction : attractions.values()) {
            attraction.exit(user);
        }
    }

    public Attraction<?> requireAttraction(String id) throws NotFoundException {
        Attraction<?> attraction = attractions.get(id);
        if (attraction == null) {
            throw new NotFoundException("attraction not found: " + id);
        }
        return attraction;
    }
}
