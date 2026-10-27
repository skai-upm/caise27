package es.upm.amusementpark.business.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import es.upm.amusementpark.business.exception.InvalidDataException;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

/**
 * Central park management. Exactly one instance exists per execution; it coordinates
 * the park state (users, and later groups, tickets and attractions), the current system
 * date and the current base ticket price.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static ParkController instance;

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private AttractionRepository attractions;
    private LocalDate currentDate;
    private BigDecimal currentPrice;

    private ParkController() {
        configure(new UserRepository(), new GroupRepository(), new TicketRepository(), new AttractionRepository());
        resetClock();
    }

    public static ParkController getInstance() {
        if (instance == null) {
            instance = new ParkController();
        }
        return instance;
    }

    /** Injects the repositories configured for the current execution. */
    public void configure(UserRepository users, GroupRepository groups, TicketRepository tickets,
            AttractionRepository attractions) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
    }

    /** Start-of-execution state: today's date and the initial base price. */
    public void resetClock() {
        this.currentDate = LocalDate.now();
        this.currentPrice = INITIAL_PRICE;
    }

    public UserRepository getUsers() {
        return users;
    }

    public GroupRepository getGroups() {
        return groups;
    }

    public AttractionRepository getAttractions() {
        return attractions;
    }

    public TicketRepository getTickets() {
        return tickets;
    }

    /** Whether any ticket dated today or later involves the user (derived, never stored). */
    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : tickets.findAll()) {
            if (ticket.involves(user) && ticket.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /** A group is locked while it has an associated group ticket dated today or later. */
    public boolean isGroupLocked(Group group) {
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group && gt.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the user belongs to any locked group. */
    public boolean isInLockedGroup(User user) {
        for (Group group : groups.findAll()) {
            if (group.contains(user) && isGroupLocked(group)) {
                return true;
            }
        }
        return false;
    }

    /** Deletes a group, clearing the reference held by its (historical) group tickets. */
    public void deleteGroup(Group group) {
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group) {
                gt.clearGroup();
            }
        }
        tickets.save();
        groups.remove(group);
    }

    /** Removes a user from every attraction it is inside. */
    public void removeFromAllAttractions(User user) {
        for (Attraction attraction : attractions.findAll()) {
            attraction.exit(user);
        }
    }

    /** Removes a user from every group it belongs to. */
    public void removeFromAllGroups(User user) {
        for (Group group : groups.findAll()) {
            group.removeMember(user);
        }
        groups.save();
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /** Sets the base price of new tickets; it must be a positive amount. */
    public void setCurrentPrice(BigDecimal currentPrice) throws InvalidDataException {
        if (currentPrice == null || currentPrice.signum() <= 0) {
            throw new InvalidDataException("Invalid price. Use a positive amount such as SYSTEM PRICE;45.50");
        }
        this.currentPrice = currentPrice;
    }
}
