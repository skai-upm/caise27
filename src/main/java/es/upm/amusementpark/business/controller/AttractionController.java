package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.AccessTicket;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.ParkFormat;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserRepository;
import java.util.List;

/**
 * Attraction operations (Singleton): creation, typed access with pass consumption, exit and queries.
 */
public final class AttractionController {

    /**
     * Users who entered and passes consumed by one entry.
     */
    public record Entry(List<User> users, List<Pass> consumedPasses) {
    }

    private static AttractionController instance;

    private Park park;
    private AttractionRepository attractions;
    private TicketRepository tickets;
    private UserRepository users;
    private PassController passes;

    private AttractionController() {
    }

    public static synchronized AttractionController getInstance() {
        if (instance == null) {
            instance = new AttractionController();
        }
        return instance;
    }

    public void configure(Park park, AttractionRepository attractions, TicketRepository tickets,
                          UserRepository users, PassController passes) {
        this.park = park;
        this.attractions = attractions;
        this.tickets = tickets;
        this.users = users;
        this.passes = passes;
    }

    public Attraction<?> create(String id, String name, String capacityText) {
        return create(id, name, capacityText, AttractionType.GENERAL.name());
    }

    public Attraction<?> create(String id, String name, String capacityText, String typeName) {
        int capacity = ParkFormat.parseCapacity(capacityText);
        AttractionType type = AttractionType.fromName(typeName);
        if (attractions.findById(id) != null) {
            throw new DuplicateException("an attraction with identifier " + id + " already exists");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.add(attraction);
        return attraction;
    }

    public Entry enter(String attractionId, String ticketId) {
        return enter(attractionId, ticketId, false);
    }

    /**
     * Lets every holder of the ticket in, consuming SpeedPasses (when {@code speed}) or the Special Pass required by
     * the attraction type. Everything is checked before any state changes.
     */
    public Entry enter(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = existing(attractionId);
        Ticket ticket = tickets.findById(ticketId);
        if (ticket == null) {
            throw new NotFoundException("no ticket with identifier " + ticketId);
        }
        if (!ticket.isValidOn(park.getDate())) {
            throw new OperationNotAllowedException("ticket " + ticketId + " is valid on "
                    + ParkFormat.formatDate(ticket.getDate()) + ", not on the current date "
                    + ParkFormat.formatDate(park.getDate()));
        }
        for (User holder : ticket.getHolders()) {
            if (!users.contains(holder)) {
                throw new OperationNotAllowedException("ticket holder " + holder.getDni()
                        + " is no longer a registered user");
            }
        }
        List<Pass> consumed = admit(attraction, ticket, speed);
        if (!consumed.isEmpty()) {
            passes.recordConsumption();
        }
        return new Entry(ticket.getHolders(), consumed);
    }

    private <T extends AccessTicket> List<Pass> admit(Attraction<T> attraction, AccessTicket ticket, boolean speed) {
        T accepted = attraction.admit(ticket);
        return attraction.enter(accepted, speed, passes);
    }

    public void exit(String attractionId, String email) {
        Attraction<?> attraction = existing(attractionId);
        for (User occupant : attraction.getOccupants()) {
            if (occupant.getEmail().equals(email)) {
                attraction.exit(occupant);
                return;
            }
        }
        throw new NotFoundException("no user with email " + email + " is inside attraction " + attractionId);
    }

    public Attraction<?> existing(String attractionId) {
        Attraction<?> attraction = attractions.findById(attractionId);
        if (attraction == null) {
            throw new NotFoundException("no attraction with identifier " + attractionId);
        }
        return attraction;
    }
}
