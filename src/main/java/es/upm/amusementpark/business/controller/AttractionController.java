package es.upm.amusementpark.business.controller;

import java.util.List;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.AttractionRepository;

/**
 * Business rules for attractions: creation, entry with any ticket type and exit.
 */
public final class AttractionController {

    private static AttractionController instance;

    private AttractionRepository attractions;
    private TicketRepository tickets;
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
    public void configure(AttractionRepository attractions, TicketRepository tickets, ParkController park) {
        this.attractions = attractions;
        this.tickets = tickets;
        this.park = park;
    }

    public Attraction find(String id) throws NotFoundException {
        Attraction attraction = attractions.findById(id);
        if (attraction == null) {
            throw new NotFoundException("Attraction not found");
        }
        return attraction;
    }

    public void create(String id, String name, int capacity) throws ParkException {
        if (capacity < 1) {
            throw new InvalidDataException("Capacity must be a positive whole number");
        }
        if (attractions.exists(id)) {
            throw new DuplicateException("Attraction already exists");
        }
        attractions.add(new Attraction(id, name, capacity));
    }

    /** Lets every holder of the ticket enter, all together or none. Returns who entered. */
    public List<User> enter(String attractionId, String ticketId) throws ParkException {
        Attraction attraction = find(attractionId);
        Ticket ticket = tickets.findById(ticketId);
        if (ticket == null) {
            throw new NotFoundException("Ticket not found");
        }
        if (!ticket.isValidOn(park.getCurrentDate())) {
            throw new OperationNotAllowedException("Ticket is not valid for the current date");
        }
        List<User> holders = ticket.holders();
        for (User user : holders) {
            if (attraction.isInside(user)) {
                throw new OperationNotAllowedException(user.getEmail() + " is already inside " + attraction.getId());
            }
        }
        if (!attraction.hasRoomFor(holders.size())) {
            throw new OperationNotAllowedException("Not enough capacity in " + attraction.getId());
        }
        for (User user : holders) {
            attraction.enter(user);
        }
        return holders;
    }

    public User exit(String attractionId, String email) throws ParkException {
        Attraction attraction = find(attractionId);
        for (User user : attraction.getOccupants()) {
            if (user.getEmail().equals(email)) {
                attraction.exit(user);
                return user;
            }
        }
        throw new NotFoundException("User is not inside " + attraction.getId());
    }
}
