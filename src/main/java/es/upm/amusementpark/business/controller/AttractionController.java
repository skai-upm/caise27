package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionAccess;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.EntryResult;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;

/**
 * Business rules for attractions: creation, entry with any ticket type and exit.
 */
public final class AttractionController {

    private static AttractionController instance;

    private AttractionRepository attractions;
    private TicketRepository tickets;
    private PassRepository passes;
    private ParkController park;

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        if (instance == null) {
            instance = new AttractionController();
        }
        return instance;
    }

    /** Injects the dependencies configured at application initialization. */
    public void configure(AttractionRepository attractions, TicketRepository tickets, PassRepository passes,
            ParkController park) {
        this.passes = passes;
        this.attractions = attractions;
        this.tickets = tickets;
        this.park = park;
    }

    public Attraction<?> find(String id) throws NotFoundException {
        Attraction<?> attraction = attractions.findById(id);
        if (attraction == null) {
            throw new NotFoundException("Attraction not found");
        }
        return attraction;
    }

    public void create(String id, String name, int capacity, AttractionType type) throws ParkException {
        if (capacity < 1) {
            throw new InvalidDataException("Capacity must be a positive whole number");
        }
        if (attractions.exists(id)) {
            throw new DuplicateException("Attraction already exists");
        }
        attractions.add(type.create(id, name, capacity));
    }

    /** Lets every holder of the ticket enter, all together or none. Returns who entered. */
    public EntryResult enter(String attractionId, String ticketId, boolean speed) throws ParkException {
        Attraction<?> attraction = find(attractionId);
        Ticket ticket = tickets.findById(ticketId);
        if (ticket == null) {
            throw new NotFoundException("Ticket not found");
        }
        return enterTyped(attraction, ticket, speed);
    }

    private <T extends AttractionAccess> EntryResult enterTyped(Attraction<T> attraction, Ticket ticket, boolean speed)
            throws ParkException {
        return attraction.admit(attraction.accessFor(ticket), park.getCurrentDate(), speed, passes::findByTicket);
    }

    public User exit(String attractionId, String email) throws ParkException {
        Attraction<?> attraction = find(attractionId);
        for (User user : attraction.getOccupants()) {
            if (user.getEmail().equals(email)) {
                attraction.exit(user);
                return user;
            }
        }
        throw new NotFoundException("User is not inside " + attraction.getId());
    }
}
